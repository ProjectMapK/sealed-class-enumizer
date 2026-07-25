import com.ncorti.ktfmt.gradle.KtfmtPlugin

// ルートは集約のみ。各サブプロジェクトの構成は各自の build.gradle.kts が持つ（docs/テストケース管理.md）。
//
// plugins ブロックは適用せず宣言のみ行う（apply false）。jvm 系と multiplatform 系のサブプロジェクトが
// それぞれの plugins DSL で KGP をロードするとクラスローダが分裂して NodeJsRootPlugin 等の共有
// build service が壊れるため、ルートのクラスローダへ一度だけロードして共有させる（親ビルドと同じ方針）。
//
// KGP のバージョンはここでのみ宣言する。settings.gradle.kts が読み込む親ビルドの version catalog を
// 唯一の源とし、サブプロジェクトはバージョン無しの kotlin("jvm") / kotlin("multiplatform") で受ける
//
// ktfmt は親ビルドと同じくルートで適用して allprojects へ配る。対象は Kotlin ソースセットと各
// プロジェクト直下の *.kts のみで、gradle-integration の TestKit フィクスチャ
// （src/test/resources/fixtures）は含まれない。フィクスチャは診断の行番号を検証する入力であり、
// 整形による行ずれが検証を壊すため対象外とする
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ktfmt)
}

allprojects {
    apply<KtfmtPlugin>()

    ktfmt { kotlinLangStyle() }
}
