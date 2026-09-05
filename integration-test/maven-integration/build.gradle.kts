import java.util.Properties

// Maven ビルドホスト（docs/test/テスト戦略.md §4）。
// resources/fixtures 配下の合成 Maven プロジェクトを実 Maven で駆動し、kotlin-maven-plugin の拡張
// としての適用（production / test 双方）・label ケースの伝達と優先順位・診断によるビルド失敗を検証する
plugins { kotlin("jvm") }

group = "io.github.projectmapk"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies { testImplementation(kotlin("test")) }

// Maven 本体はビルドの依存として取得する（ホストへの Maven インストールを前提にしない）。
// 版は親ビルドの version catalog を正とし、maven-plugin がコンパイル時に参照する Maven API と揃える
val mavenVersion: String = libs.versions.maven.get()

val mavenDistribution = configurations.dependencyScope("mavenDistribution")

val mavenDistributionFiles =
    configurations.resolvable("mavenDistributionFiles") { extendsFrom(mavenDistribution.get()) }

dependencies { add(mavenDistribution.name, "org.apache.maven:apache-maven:$mavenVersion:bin@zip") }

val mavenDistRoot = layout.buildDirectory.dir("maven-dist")

// 配布物の展開。起動は bin/mvn ではなく classworlds の Launcher を直接叩くため（MavenHarness）、
// zip 展開で失われる実行権限に依存しない
val unpackMaven =
    tasks.register<Sync>("unpackMaven") {
        from(zipTree(provider { mavenDistributionFiles.get().singleFile }))
        into(mavenDistRoot)
    }

// フィクスチャのプレースホルダ置換（MavenHarness）へ渡す版。自版は親ビルドの gradle.properties、
// Kotlin 版は共有カタログを正とする（docs/test/フィクスチャ構成.md §7）
val enumizerOwnVersion: String =
    Properties()
        .apply { rootDir.resolve("../gradle.properties").inputStream().use(::load) }
        .getProperty("enumizerVersion") ?: error("../gradle.properties に enumizerVersion が無い")

val enumizerKotlinVersion: String = libs.versions.kotlin.get()

// Maven のローカルリポジトリはテスト専用の場所へ隔離する（docs/test/フィクスチャ構成.md §7）。
// 既定の ~/.m2/repository を使うと、Maven が取得する Gradle module metadata を伴わない成果物が
// そこへ入り、mavenLocal() を宣言する TestKit フィクスチャの variant 解決を壊す
val mavenLocalRepo = layout.buildDirectory.dir("maven-local-repo")

// 本プラグインの成果物は publishToMavenLocal の出力（= ローカル Maven）を正とし、
// 隔離リポジトリへは MavenHarness が複製する
val publishedRepo = providers.systemProperty("user.home").map { "$it/.m2/repository" }

// フィクスチャの展開先。失敗解析のため実行後は残し、次回の実行開始時に作り直す
// （docs/test/フィクスチャ構成.md §7（展開先は実行前に回収する））
val fixtureWorkRoot = layout.buildDirectory.dir("maven-fixtures")

val cleanFixtureWorkRoot =
    tasks.register("cleanFixtureWorkRoot") {
        val workRoot = fixtureWorkRoot
        doLast { workRoot.get().asFile.deleteRecursively() }
    }

tasks.test {
    dependsOn(cleanFixtureWorkRoot, unpackMaven)
    // フィクスチャはプラグイン一式をローカル Maven から解決するため、テスト前に公開しておく
    // （docs/test/フィクスチャ構成.md §7 の local-repo 経路）
    dependsOn(gradle.includedBuild("sealed-class-enumizer").task(":publishAllToMavenLocal"))
    useJUnitPlatform()
    systemProperty(
        "enumizer.mavenHome",
        mavenDistRoot.get().asFile.resolve("apache-maven-$mavenVersion").absolutePath,
    )
    systemProperty("enumizer.mavenLocalRepo", mavenLocalRepo.get().asFile.absolutePath)
    systemProperty("enumizer.publishedRepo", publishedRepo.get())
    systemProperty("enumizer.fixtureWorkRoot", fixtureWorkRoot.get().asFile.absolutePath)
    systemProperty("enumizer.version", "$enumizerKotlinVersion-$enumizerOwnVersion")
    systemProperty("enumizer.kotlinVersion", enumizerKotlinVersion)
    // Maven のローカルリポジトリは共有資源であり、並行ダウンロードでの破損を避けるため直列に回す
    // （フィクスチャは 1 プロジェクトずつで、TestKit のような多重化の利得も無い）
    maxParallelForks = 1
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}
