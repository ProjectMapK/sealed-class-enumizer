// 新バイナリ v2（3 末端）。実行時にのみ classpath へ差し替える
plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

// 差し替え検証用に jar 名をバージョン表記なしで固定する
tasks.jar {
    archiveFileName.set("producer-v2.jar")
}
