// ルートは集約のみ。各サブプロジェクトの構成は各自の build.gradle.kts が持つ（docs/テストケース管理.md）。
//
// plugins ブロックは適用せず宣言のみ行う（apply false）。jvm 系と multiplatform 系のサブプロジェクトが
// それぞれの plugins DSL で KGP をロードするとクラスローダが分裂して NodeJsRootPlugin 等の共有
// build service が壊れるため、ルートのクラスローダへ一度だけロードして共有させる（親ビルドと同じ方針）。
plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
}
