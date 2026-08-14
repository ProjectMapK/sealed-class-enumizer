// Java 消費側（docs/test/テスト戦略.md §4・docs/概要.md §3「Java からの利用」）。
// Java 21 パターンマッチング switch（JEP 441）のためこのモジュールだけ toolchain 21 を使う
plugins { java }

group = "io.github.projectmapk"

version = "1.0-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

dependencies {
    testImplementation(project(":producer-jvm"))
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }
