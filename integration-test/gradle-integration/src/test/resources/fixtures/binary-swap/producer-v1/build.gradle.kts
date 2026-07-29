// 旧バイナリ v1（2 末端）。consumer のコンパイル対象
plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

// 差し替え検証用に jar 名をバージョン表記なしで固定する
tasks.jar {
    archiveFileName.set("producer-v1.jar")
}
