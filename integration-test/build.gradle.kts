// ルートは集約のみ。各サブプロジェクトの構成は各自の build.gradle.kts が持つ（docs/テストケース管理.md）。
//
// plugins ブロックは適用せず宣言のみ行う（apply false）。jvm 系と multiplatform 系のサブプロジェクトが
// それぞれの plugins DSL で KGP をロードするとクラスローダが分裂して NodeJsRootPlugin 等の共有
// build service が壊れるため、ルートのクラスローダへ一度だけロードして共有させる（親ビルドと同じ方針）。
//
// KGP のバージョンはここでのみ宣言する。settings.gradle.kts が読み込む親ビルドの version catalog を
// 唯一の源とし、サブプロジェクトはバージョン無しの kotlin("jvm") / kotlin("multiplatform") で受ける
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}
