import io.github.projectmapk.sealedClassEnumizer.gradle.LabelCase

plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies { testImplementation(kotlin("test")) }

tasks.test { useJUnitPlatform() }

// プロジェクト既定を SNAKE_CASE へ変更する（docs/test/ケース06-ビルド動態.md BLD-48）
sealedClassEnumizer { labelCase = LabelCase.SNAKE_CASE }
