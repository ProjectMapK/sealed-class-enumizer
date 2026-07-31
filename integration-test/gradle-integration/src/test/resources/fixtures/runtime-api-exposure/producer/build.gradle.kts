// 生成側（プラグイン適用）。既定は runtime-api を api スコープで自動公開する。
// -PhideRuntimeApi 指定時は自動追加を無効化し、runtime-api を implementation で「隠した」縮退にする
// （api 公開前の状況＝runtime-api が利用側へ伝播しない状態を意図的に再現する。docs/test/ケース06-ビルド動態.md BLD-41）
plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

if (project.hasProperty("hideRuntimeApi")) {
    sealedClassEnumizer {
        addRuntimeDependency.set(false)
    }
    dependencies {
        implementation("io.github.projectmapk:sealed-class-enumizer-runtime-api:%%ENUMIZER_VERSION%%")
    }
}
