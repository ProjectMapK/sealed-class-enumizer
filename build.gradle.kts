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

// 版は次の 2 段で単一情報源から導出する。上流ほど源に近く、下流は上流の値を写した生成物として扱う。
//   1. kotlin-maven-plugin の親 POM の <maven.version> → version catalog の maven 版（syncMavenVersion）
//   2. version catalog / enumizerVersion / wrapper properties → README / docs / .idea 中の現行版表記
//      （syncVersionMentions）
// カタログの値は Gradle の構成時に読まれるため、1 の書き戻しを 2 へ反映するには起動を分ける必要がある。
// Dependabot 等の版更新はマニフェストしか書き換えず下流が置き去りになるため、
// check へ紐付けた checkMavenVersion / checkVersionMentions が乖離を検出し、同期タスクが書き換える
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
    val childPom = pomOf("org.jetbrains.kotlin:kotlin-maven-plugin:$kotlinCurrent@pom")
    val parentPom = pomOf("org.jetbrains.kotlin:kotlin-project:$kotlinCurrent@pom")
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

val versionCatalogFile = layout.projectDirectory.file("gradle/libs.versions.toml").asFile

val mavenVersionEntry = Regex("""^maven = "[^"]*"$""", RegexOption.MULTILINE)

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
        val declared = mavenCurrent
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

// 置換は「現行版を指す表記」に限る。サポート下限（"Kotlin 2.4" / "Gradle 9+" / "Maven 3.9+"）・
// 特定版に固定した実測エビデンス（設計00 の "Kotlin 2.4.0 実測"）・別版での版形式の例示は
// 書き換えてはならないため、対象ファイルの許可リストと文脈付きパターンの両方で絞る。
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
// 現行版への言及を持つ資料を増やした場合はここへ追加する。
// ただし過去の版を記録する文書（CHANGELOG）は対象にしない。履歴の版が現行版へ書き換えられてしまう
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
// （docs/test/フィクスチャ構成.md §4 の local-repo 経路）。ローカル Maven を使うのは
// 非タイムスタンプの SNAPSHOT が上書き公開され、Gradle が成果物をキャッシュしないため
tasks.register("publishAllToMavenLocal") {
    dependsOn(subprojects.map { "${it.path}:publishToMavenLocal" })
}
