import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory
import java.util.Properties

// Gradle TestKit ホスト（docs/test/テスト戦略.md §4）。
// resources/fixtures 配下の合成ビルドを GradleRunner で駆動し、IC 回帰・決定性・
// 跨モジュール負値診断・ABI 伝播・旧バイナリ差し替え・基底不在ラウンドを検証する
plugins { kotlin("jvm") }

group = "io.github.projectmapk"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

// フィクスチャのプレースホルダ置換（TestKitHarness）へ渡す版。自版は親ビルドの gradle.properties、
// Kotlin 版は共有カタログを正とする（docs/test/フィクスチャ構成.md §4）
val enumizerOwnVersion: String =
    Properties()
        .apply { rootDir.resolve("../gradle.properties").inputStream().use(::load) }
        .getProperty("enumizerVersion") ?: error("../gradle.properties に enumizerVersion が無い")

val enumizerKotlinVersion: String = libs.versions.kotlin.get()

// 実行ホストの物理メモリ（GiB）。標準の OperatingSystemMXBean は物理メモリを持たず HotSpot 拡張が要るため、
// それを備えない JVM では null を返し、同時実行数をコア数だけで決めさせる
fun hostPhysicalMemoryGb(): Int? =
    (ManagementFactory.getOperatingSystemMXBean() as? OperatingSystemMXBean)?.let {
        (it.totalMemorySize / (1024L * 1024L * 1024L)).toInt()
    }

// フィクスチャの展開先（IcTestSupport が使う）。テスト毎に一意なディレクトリを掘るため、
// 実行を重ねると際限なく溜まってテスト時間が実行毎に悪化する。失敗解析のため実行後は残し、
// 次回の実行開始時に作り直す（docs/test/フィクスチャ構成.md §4（フィクスチャ展開先の回収））
val fixtureWorkRoot = layout.buildDirectory.dir("testkit-fixtures")

// 並行実行するフィクスチャビルドへ配る Gradle ユーザーホームの置き場（TestKitHarness が
// スレッド毎に 1 つ掘る）。中身は Gradle が生成するキャッシュであり、作り直すと生成し直しになるため、
// フィクスチャ展開先と違って実行毎の回収はしない
val testKitHomeRoot = layout.buildDirectory.dir("testkit-homes")

// フィクスチャビルドのデーモンへ与えるヒープ。Gradle の既定（-Xmx512m）は KGP を載せるには不足する。
// TestKitHarness がフィクスチャの gradle.properties へ書き、下の同時実行数の算出にも使うため、
// 宣言はここだけに置く
val fixtureDaemonHeapGb = 2

// TestKit の 1 テストは別プロセスの Gradle デーモンと Kotlin デーモンを 1 本ずつ占有するため、
// 1 つの同時実行がデーモンヒープ 2 本分のメモリを確保する。同時実行数はこれとホストの資源から決める
// （`-PtestKitParallelism` で上書き可）
val memoryPerBuildGb = fixtureDaemonHeapGb * 2

val cpuBoundParallelism = Runtime.getRuntime().availableProcessors()

val memoryBoundParallelism = hostPhysicalMemoryGb()?.div(memoryPerBuildGb) ?: cpuBoundParallelism

val testKitParallelism =
    providers
        .gradleProperty("testKitParallelism")
        .map(String::toInt)
        .getOrElse(minOf(cpuBoundParallelism, memoryBoundParallelism).coerceAtLeast(1))

// 展開先の作り直しはテスト本体と分けて前段のタスクで行う（test の出力準備と混ざらないようにする）。
// 直前の実行が残した TestKit のデーモンが Windows でファイルを掴んだままのことがあるため、
// 削除は best-effort とする（掴まれた分は次回以降の実行で回収される）
val cleanFixtureWorkRoot =
    tasks.register("cleanFixtureWorkRoot") {
        val workRoot = fixtureWorkRoot
        doLast { workRoot.get().asFile.deleteRecursively() }
    }

tasks.test {
    dependsOn(cleanFixtureWorkRoot)
    // フィクスチャはプラグイン一式をローカル Maven から解決するため、テスト前に公開しておく
    // （docs/test/フィクスチャ構成.md §4 の local-repo 経路）
    dependsOn(gradle.includedBuild("sealed-class-enumizer").task(":publishAllToMavenLocal"))
    useJUnitPlatform()
    systemProperty("enumizer.fixtureWorkRoot", fixtureWorkRoot.get().asFile.absolutePath)
    systemProperty("enumizer.testKitHomeRoot", testKitHomeRoot.get().asFile.absolutePath)
    // フィクスチャビルドのホームを分けると依存キャッシュもホーム毎になるため、親ビルドの依存キャッシュを
    // 読み取り専用で共有させる（GradleRunner は本テスト JVM の環境変数を引き継ぐ）
    environment("GRADLE_RO_DEP_CACHE", gradle.gradleUserHomeDir.resolve("caches").absolutePath)
    systemProperty("enumizer.version", "$enumizerKotlinVersion-$enumizerOwnVersion")
    systemProperty("enumizer.kotlinVersion", enumizerKotlinVersion)
    // 並行実行は JUnit Platform がテストメソッド単位で行う（方針と静的な設定は
    // src/test/resources/junit-platform.properties）。Gradle 側のフォークはクラス単位でしか分配できず
    // ノブが二重になるため 1 本に固定する
    maxParallelForks = 1
    systemProperty("enumizer.fixtureDaemonHeapGb", fixtureDaemonHeapGb)
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", testKitParallelism)
    // parallelism だけでは上限にならない。JUnit の実行基盤は ForkJoinPool であり、クラスが子テストの
    // 完了を待つ間に補償スレッドを起こすため、既定の最大プールサイズ（parallelism + 256）まで
    // 同時実行が膨らむ。最大プールサイズを parallelism へ揃えて頭打ちにする
    systemProperty(
        "junit.jupiter.execution.parallel.config.fixed.max-pool-size",
        testKitParallelism,
    )
    // テスト JVM が抱えるのは実行中のビルド出力（BuildResult.output）と、診断系がクラス内で共有する
    // 分だけで、テストワーカーの既定で足りる。既定値の変更に左右されないよう同値を明示する
    maxHeapSize = "512m"
    // TestKit ビルドは長時間になるためテスト毎の結果を逐次流す
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}
