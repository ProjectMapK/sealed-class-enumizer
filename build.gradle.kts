import com.ncorti.ktfmt.gradle.KtfmtPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.plugins.signing.SigningExtension

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

// 署名を必須とする条件を、リモートリポジトリへ publish する場合に限る。
// 公開プラグインの既定はリリース版であれば必須で、その判定はローカル公開にも及ぶ。
// integration-test のフィクスチャは鍵の無い環境で publishToMavenLocal を使うため、そのままでは
// リリース版を宣言した時点でローカル公開が成立しなくなる（ローカル公開のタスクはリモートとは別型）。
// 未署名のまま公開へ進むことは、公開先の検証が受け付けないことで防がれる。
// 設定は各モジュールのスクリプト評価中に書かれるため、上書きは評価後に行う
allprojects {
    plugins.withId("signing") {
        afterEvaluate {
            extensions.configure<SigningExtension> {
                setRequired {
                    !version.toString().endsWith("-SNAPSHOT") &&
                        gradle.taskGraph.allTasks.any { it is PublishToMavenRepository }
                }
            }
        }
    }
}

// 版は次の 2 段で単一情報源から導出する。上流ほど源に近く、下流は上流の値を写した生成物として扱う。
//   1. kotlin-maven-plugin の親 POM の <maven.version> → version catalog の maven 版（syncMavenVersion）
//   2. version catalog / enumizerVersion → README / docs / .idea 中の現行版表記（syncVersionMentions）
// カタログの値は Gradle の構成時に読まれるため、1 の書き戻しを 2 へ反映するには起動を分ける必要がある。
// Dependabot 等の版更新はマニフェストしか書き換えず下流が置き去りになるため、
// check へ紐付けた checkMavenVersion / checkVersionMentions が乖離を検出し、同期タスクが書き換える
fun minorOf(version: String) = version.split(".").take(2).joinToString(".")

val versionCatalogFile = layout.projectDirectory.file("gradle/libs.versions.toml").asFile

val kotlinVersionEntry = Regex("""^kotlin = "([^"]*)"$""", RegexOption.MULTILINE)

val mavenVersionEntry = Regex("""^maven = "([^"]*)"$""", RegexOption.MULTILINE)

// 整合の機構が源とするのはカタログの宣言そのものであり、解決後の値ではない。
// マイナー横断ビルドが与える -PkotlinVersionOverride は解決値だけを差し替えるもので、
// カタログの宣言も、それを写した資料も変わらない。解決値を源にすると、宣言と一致した資料を
// 乖離と見なして検査が落ち、同期タスクは宣言に無い版を資料や上流 POM の導出へ持ち込む
fun declaredVersion(entry: Regex): String =
    checkNotNull(entry.find(versionCatalogFile.readText())) {
            "gradle/libs.versions.toml に版の宣言がありません: $entry"
        }
        .groupValues[1]

val kotlinDeclaredVersion = declaredVersion(kotlinVersionEntry)
val mavenDeclaredVersion = declaredVersion(mavenVersionEntry)
val enumizerDeclaredVersion = providers.gradleProperty("enumizerVersion").get()
val enumizerReleaseVersion = enumizerDeclaredVersion.removeSuffix("-SNAPSHOT")
// リリース版を宣言している状態か（開発中は -SNAPSHOT が付く）。配布物を説明する文書の同期対象を切り替える
val isReleaseVersion = enumizerDeclaredVersion == enumizerReleaseVersion

// maven-core は kotlin-maven-plugin の realm 側が実行時に供給するため compileOnly で参照する。
// その版は kotlin-maven-plugin 自身が provided スコープで宣言する版が正だが、provided は Gradle の
// POM 取り込み対象外で推移解決されないため、カタログのリテラルとして持つ必要がある。
// リテラルは手で選ばず、kotlin-maven-plugin の親 POM が持つ <maven.version> から導出する
// （syncMavenVersion が書き戻し、checkMavenVersion が乖離を検出する）。
// 親 POM は座標を決め打ちで解決したうえで、子 POM の <parent> と照合し、
// 上流のレイアウト変更を黙って取り込まないようにする

// POM 自体はビルドの依存ではなく参照する入力に過ぎないため、detached configuration で取得する
// （名前付きの構成として公開すると依存グラフの登録対象にも入ってしまう）
fun pomOf(notation: String): FileCollection =
    configurations
        .detachedConfiguration(dependencies.create(notation))
        .apply { isTransitive = false }
        .incoming
        .files

// POM の解決を伴うため、値の取り出しは構成時ではなく実行時に行う
val upstreamMavenVersion: Provider<String> = run {
    val childPom = pomOf("org.jetbrains.kotlin:kotlin-maven-plugin:$kotlinDeclaredVersion@pom")
    val parentPom = pomOf("org.jetbrains.kotlin:kotlin-project:$kotlinDeclaredVersion@pom")
    providers.provider {
        val child = childPom.singleFile.readText()
        val parentBlock =
            checkNotNull(
                    Regex("""<parent>(.*?)</parent>""", RegexOption.DOT_MATCHES_ALL).find(child)
                ) {
                    "kotlin-maven-plugin の POM に <parent> がありません"
                }
                .groupValues[1]
        val parentArtifact =
            checkNotNull(Regex("""<artifactId>([^<]+)</artifactId>""").find(parentBlock)) {
                    "kotlin-maven-plugin の POM の <parent> に artifactId がありません"
                }
                .groupValues[1]
        check(parentArtifact == "kotlin-project") {
            "kotlin-maven-plugin の親 POM が $parentArtifact へ変わっています。導出元の座標を見直してください"
        }
        checkNotNull(
                Regex("""<maven\.version>([^<]+)</maven\.version>""")
                    .find(parentPom.singleFile.readText())
            ) {
                "親 POM に <maven.version> がありません"
            }
            .groupValues[1]
    }
}

tasks.register("syncMavenVersion") {
    val upstream = upstreamMavenVersion
    val file = versionCatalogFile
    val entry = mavenVersionEntry
    doLast {
        val current = file.readText()
        val synced = entry.replace(current, "maven = \"${upstream.get()}\"")
        if (synced != current) file.writeText(synced)
    }
}

val checkMavenVersion =
    tasks.register("checkMavenVersion") {
        val upstream = upstreamMavenVersion
        val declared = mavenDeclaredVersion
        doLast {
            val expected = upstream.get()
            if (declared != expected) {
                throw GradleException(
                    "maven 版が kotlin-maven-plugin の参照版とズレています（宣言 $declared / 上流 $expected）。" +
                        "./gradlew syncMavenVersion で追随してください"
                )
            }
        }
    }

// 追随の対象は配布物を左右する版、すなわち Kotlin 版と自版に限る。
// ビルドツール（Gradle / Maven）の版は配布物へ影響しないため、資料はサポート下限
// （"Gradle 9+" / "Maven 3.9+"）のみを書き、具体的な版へは言及しない。言及した場合、
// 配布物と無関係なラッパー・ハーネスの更新が資料の追随を要求し、その更新 PR が CI で落ちる。
//
// 置換は「現行版を指す表記」に限る。サポート下限（"Kotlin 2.4"）・特定版に固定した実測エビデンス
// （設計00 の "Kotlin 2.4.0 実測"）・別版での版形式の例示は書き換えてはならないため、
// 対象ファイルの許可リストと文脈付きパターンの両方で絞る。
// パッチまでの表記とマイナーまでの表記は、それぞれの粒度を保ったまま現行値へ置換する
val versionMentionRules: List<Pair<Regex, String>> =
    listOf(
        // 配布版のフル形式 <KotlinVersion>-<自版>（README のセットアップ例・版形式の現行例）
        Regex("""(?<![\d.])\d+\.\d+\.\d+-\d+\.\d+\.\d+(?![\d.-])""") to
            "$kotlinDeclaredVersion-$enumizerReleaseVersion",
        // README のセットアップ例が指定する KGP 版
        Regex("""(?<=kotlin\("(?:multiplatform|jvm)"\) version ")\d+\.\d+\.\d+(?=")""") to
            kotlinDeclaredVersion,
        // .idea/kotlinc.xml（IDE が同期時に書き戻す Kotlin 版。PR 側で揃え、同期後の作業ツリー差分を防ぐ）
        Regex("""(?<=name="version" value=")\d+\.\d+\.\d+(?=")""") to kotlinDeclaredVersion,
        // 文中の現行 Kotlin 版（3 成分のみ。2 成分のサポート下限 "Kotlin 2.4" には一致しない）
        Regex("""(?<=Kotlin )\d+\.\d+\.\d+(?![\d.-])""") to kotlinDeclaredVersion,
        // 対応マイナーの表記（README の互換表の "2.4.x"）
        Regex("""\d+\.\d+\.x""") to "${minorOf(kotlinDeclaredVersion)}.x",
    )

// 対象は文書の性質で 2 群に分ける。現行版への言及を持つ資料を増やした場合は該当する群へ追加する。
// ただし過去の版を記録する文書（CHANGELOG）はどちらにも入れない。履歴の版が現行版へ書き換えられてしまう。
//
// 配布物を説明する文書（README のセットアップ例・互換表・概要 §7 の版形式）。表記が指すのは
// 利用者が実際に入手できる版であり、開発中に追随させると未公開の版を案内してしまう。
// このためリリース版を宣言している間だけ対象とし、開発中は据え置く
val releaseVersionMentionTargets = listOf("README.md", "docs/概要.md")

// 現在の開発・検証環境を記録する文書と、IDE が同期時に書き戻す設定。
// カタログの更新へ即座に追随させる（追随しないと作業ツリーへ差分が残る）
val environmentVersionMentionTargets =
    listOf(
        "docs/実装ノート.md",
        "docs/test/テスト戦略.md",
        "docs/エッジケースへの対応方針.md",
        ".idea/kotlinc.xml",
    )

val versionMentionTargets =
    (environmentVersionMentionTargets +
            releaseVersionMentionTargets.takeIf { isReleaseVersion }.orEmpty())
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

tasks.named("check") { dependsOn(checkVersionMentions, checkMavenVersion) }

// gradle.properties の kotlin.code.style=official に合わせ、ktfmt も Kotlin 公式スタイル
// （ブロック・継続ともインデント 4、末尾カンマ付与）で揃える
allprojects {
    apply<KtfmtPlugin>()

    ktfmt { kotlinLangStyle() }
}

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
    // 版切替 UI を出すための Dokka プラグイン（HTML 形式のみが対象）。版指定は要らず、適用中の Dokka へ揃う
    dokkaHtmlPlugin("org.jetbrains.dokka:versioning-plugin")
}

// 過去版の出力は保管場所から展開したディレクトリを -PolderDocsDir で渡す（版毎のサブディレクトリを持つ親を指す）。
// 指定が無い場合は現行版だけの出力になるため、ローカルでの生成は保管場所を用意せずに行える
dokka {
    pluginsConfiguration {
        versioning {
            version = enumizerFullVersion
            providers.gradleProperty("olderDocsDir").orNull?.let {
                olderVersionsDir = layout.projectDirectory.dir(it)
            }
        }
    }
}

// integration-test の TestKit フィクスチャ向けに、3 モジュールのローカル Maven 公開を集約する
// （docs/test/フィクスチャ構成.md §5 の local-repo 経路）。ローカル Maven を使うのは
// 非タイムスタンプの SNAPSHOT が上書き公開され、Gradle が成果物をキャッシュしないため
tasks.register("publishAllToMavenLocal") {
    dependsOn(subprojects.map { "${it.path}:publishToMavenLocal" })
}
