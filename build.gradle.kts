import com.ncorti.ktfmt.gradle.KtfmtPlugin

// ルートプロジェクトは集約のみを担う。各モジュールの構成は各自の build.gradle.kts が持つ。
// integration-test は独立した composite build であり、ここには含めない（docs/test/テスト戦略.md）。
//
// plugins ブロックは適用せず宣言のみ行う（apply false）。サブプロジェクト毎に異なるプラグイン集合を
// 要求するとクラスローダが分裂し、KGP の共有 build service（KotlinNativeBundleBuildService）が
// 型不一致になって IDE sync（prepareKotlinIdeaImport → commonizeNativeDistribution）が失敗するため、
// 全プラグインをルートのクラスローダへ一度だけロードして共有させる。
// ktfmt はモジュール構成ではなくビルド全体の共通規約であり、スタイル定義を 1 か所に保つため
// ルートで適用して allprojects へ配る（ルート自身の build.gradle.kts / settings.gradle.kts も対象になる）。
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.autoservice) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.gradle.plugin.publish) apply false
    // ルートは API ドキュメントの集約点として適用する（下の dokka 依存）
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktfmt)
}

// 公開 group と版の単一情報源。版は <KotlinVersion>-<自版> 形式（docs/概要.md §7 のマイナー毎分割の実現）で、
// Kotlin 版は version catalog・自版は gradle.properties の enumizerVersion が正。
// 開発中の自版は -SNAPSHOT を維持する（ローカル Maven の上書き公開・非キャッシュという
// integration-test の local-repo 経路の前提のため）
val enumizerFullVersion =
    "${libs.versions.kotlin.get()}-${providers.gradleProperty("enumizerVersion").get()}"

allprojects {
    group = "io.github.projectmapk"
    version = enumizerFullVersion
}

// README / docs / .idea 中の「現行バージョン」表記を単一情報源（Kotlin / Maven = version catalog・
// 自版 = enumizerVersion・Gradle = wrapper properties）から導出した値へ揃える。
// Dependabot 等の版更新はマニフェストしか書き換えず表記が置き去りになるため、
// checkVersionMentions（check へ紐付け。CI では version-mentions ジョブ）が乖離を検出し、
// syncVersionMentions が書き換える。
// 置換は「現行版を指す表記」に限る。サポート下限（"Kotlin 2.4" / "Gradle 9+" / "Maven 3.9+"）・
// 特定版に固定した実測エビデンス（設計00 の "Kotlin 2.4.0 実測"）・別版での版形式の例示は
// 書き換えてはならないため、対象ファイルの許可リストと文脈付きパターンの両方で絞る
fun minorOf(version: String) = version.split(".").take(2).joinToString(".")

val kotlinCurrent = libs.versions.kotlin.get()
val mavenCurrent = libs.versions.maven.get()
val enumizerReleaseVersion =
    providers.gradleProperty("enumizerVersion").get().removeSuffix("-SNAPSHOT")
val gradleWrapperVersion =
    providers
        .fileContents(layout.projectDirectory.file("gradle/wrapper/gradle-wrapper.properties"))
        .asText
        .map { checkNotNull(Regex("""/gradle-(\d+(?:\.\d+)+)-""").find(it)).groupValues[1] }
        .get()

// 3 成分（パッチ付き）と 2 成分（マイナーまで）の表記は、それぞれの粒度を保ったまま現行値へ置換する
val versionMentionRules: List<Pair<Regex, String>> =
    listOf(
        // 配布版のフル形式 <KotlinVersion>-<自版>（README のセットアップ例・版形式の現行例）
        Regex("""(?<![\d.])\d+\.\d+\.\d+-\d+\.\d+\.\d+(?![\d.-])""") to
            "$kotlinCurrent-$enumizerReleaseVersion",
        // README のセットアップ例が指定する KGP 版
        Regex("""(?<=kotlin\("(?:multiplatform|jvm)"\) version ")\d+\.\d+\.\d+(?=")""") to
            kotlinCurrent,
        // .idea/kotlinc.xml（IDE が同期時に書き戻す Kotlin 版。PR 側で揃え、同期後の作業ツリー差分を防ぐ）
        Regex("""(?<=name="version" value=")\d+\.\d+\.\d+(?=")""") to kotlinCurrent,
        // 文中の現行 Kotlin 版（3 成分のみ。2 成分のサポート下限 "Kotlin 2.4" には一致しない）
        Regex("""(?<=Kotlin )\d+\.\d+\.\d+(?![\d.-])""") to kotlinCurrent,
        // 対応マイナーの表記（README の互換表の "2.4.x"）
        Regex("""\d+\.\d+\.x""") to "${minorOf(kotlinCurrent)}.x",
        // Gradle / Maven。下限表記（"9+" / "3.9+"）は後置の + により対象外となる
        Regex("""(?<=Gradle )\d+\.\d+\.\d+(?![\d.+])""") to gradleWrapperVersion,
        Regex("""(?<=Gradle )\d+\.\d+(?![\d.+])""") to minorOf(gradleWrapperVersion),
        Regex("""(?<=Maven )\d+\.\d+\.\d+(?![\d.+])""") to mavenCurrent,
        Regex("""(?<=Maven )\d+\.\d+(?![\d.+])""") to minorOf(mavenCurrent),
    )

// 対象箇所: README（セットアップ例・互換表・検証済みビルド環境）・概要 §7（版形式の現行例）・
// 実装ノート / テスト戦略 / エッジケースへの対応方針（実測・テスト環境の宣言）・kotlinc.xml。
// 現行版への言及を持つ資料を増やした場合はここへ追加する
val versionMentionTargets =
    listOf(
            "README.md",
            "docs/概要.md",
            "docs/実装ノート.md",
            "docs/test/テスト戦略.md",
            "docs/エッジケースへの対応方針.md",
            ".idea/kotlinc.xml",
        )
        .map { it to layout.projectDirectory.file(it).asFile }

tasks.register("syncVersionMentions") {
    val targets = versionMentionTargets
    val rules = versionMentionRules
    doLast {
        targets.forEach { (_, file) ->
            val current = file.readText()
            val synced = rules.fold(current) { text, (regex, value) -> regex.replace(text, value) }
            if (synced != current) file.writeText(synced)
        }
    }
}

val checkVersionMentions =
    tasks.register("checkVersionMentions") {
        val targets = versionMentionTargets
        val rules = versionMentionRules
        doLast {
            val stale = targets.filter { (_, file) ->
                val current = file.readText()
                rules.fold(current) { text, (regex, value) -> regex.replace(text, value) } !=
                    current
            }
            if (stale.isNotEmpty()) {
                throw GradleException(
                    "現行バージョンとズレた表記があります: ${stale.joinToString { it.first }}。" +
                        "./gradlew syncVersionMentions で追随してください"
                )
            }
        }
    }

tasks.named("check") { dependsOn(checkVersionMentions) }

// gradle.properties の kotlin.code.style=official に合わせ、ktfmt も Kotlin 公式スタイル
// （ブロック・継続ともインデント 4、末尾カンマ付与）で揃える
allprojects {
    apply<KtfmtPlugin>()

    ktfmt { kotlinLangStyle() }
}

// ktfmt はプロジェクト毎にタスクを登録するため、ルートのタスク（:ktfmtFormat / :ktfmtCheck）が
// 対象とするのはルート直下の *.kts のみで、サブプロジェクトも独立したビルドである integration-test も
// 入らない。作業ツリー全体をルート指定の 1 コマンドで扱えるよう、両者をルートのタスクへ束ねる。
// integration-test は別ビルドのためタスク依存では辿れず、GradleBuild で入れ子のビルドとして起動する
// ktfmt はプロジェクト毎にタスクを登録するため、ルートを指定した :ktfmtFormat / :ktfmtCheck が
// 対象とするのはルート直下の *.kts に限られる。ルートのタスクを集約点として各サブプロジェクトへ束ね、
// ルート指定でルートビルド全体が対象になるようにする
listOf("ktfmtFormat", "ktfmtCheck").forEach { taskName ->
    tasks.named(taskName) { dependsOn(subprojects.map { "${it.path}:$taskName" }) }
}

// integration-test は独立したビルドであり、上の集約でも allprojects でも辿れない。仕上げの整形を
// ルート指定の 1 コマンドで完結させるため、ktfmtFormat からラッパー経由の別プロセスとして起動する
// （入れ子のビルドを組む GradleBuild は、integration-test が親ビルドを includeBuild しているため
// 「Cannot include build」となり使えない）。ラッパーと -p の組み合わせは CI が integration-test を
// driving する形と同じで、Gradle 本体の版もルートのラッパーへ揃う。
// 検証側（ktfmtCheck）を束ねないのは、それが check 経由でルートの build へ載り、ルートビルドの
// 検証が integration-test 側の構成・依存解決の失敗に巻き込まれるため。未整形の検出はビルド毎の
// check が担い、CI もビルド毎にジョブを分けている
val ktfmtFormatIntegrationTest =
    tasks.register<Exec>("ktfmtFormatIntegrationTest") {
        val wrapper =
            providers.systemProperty("os.name").map {
                if (it.startsWith("Windows")) "gradlew.bat" else "gradlew"
            }
        commandLine(
            layout.projectDirectory.file(wrapper.get()).asFile.absolutePath,
            "-p",
            "integration-test",
            "ktfmtFormat",
        )
    }

tasks.named("ktfmtFormat") { dependsOn(ktfmtFormatIntegrationTest) }

// API ドキュメント（GitHub Pages 掲載用）の集約。対象は利用者がコードから触る公開 API を持つ 2 モジュールで、
// compiler-plugin（内部実装）と maven-plugin（利用面は POM の設定であり Kotlin API ではない）は含めない。
// 集約出力は build/dokka/html
dependencies {
    dokka(project(":sealed-class-enumizer-runtime-api"))
    dokka(project(":sealed-class-enumizer-gradle-plugin"))
}

// integration-test の TestKit フィクスチャ向けに、3 モジュールのローカル Maven 公開を集約する
// （docs/test/フィクスチャ構成.md §4 の local-repo 経路）。ローカル Maven を使うのは
// 非タイムスタンプの SNAPSHOT が上書き公開され、Gradle が成果物をキャッシュしないため
tasks.register("publishAllToMavenLocal") {
    dependsOn(subprojects.map { "${it.path}:publishToMavenLocal" })
}
