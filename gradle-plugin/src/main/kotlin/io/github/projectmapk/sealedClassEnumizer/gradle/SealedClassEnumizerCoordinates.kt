package io.github.projectmapk.sealedClassEnumizer.gradle

// GROUP / VERSION / アーティファクト名はビルドスクリプト（group / version / 各モジュールの公開 artifactId）と、
// COMPILER_PLUGIN_ID は compiler-plugin の EnumizeCommandLineProcessor.PLUGIN_ID と一致していなければならない
object SealedClassEnumizerCoordinates {
    const val GROUP: String = "io.github.projectmapk"
    const val VERSION: String = "1.0-SNAPSHOT"
    const val COMPILER_PLUGIN_ARTIFACT: String = "sealed-class-enumizer-compiler-plugin"
    const val RUNTIME_API_ARTIFACT: String = "sealed-class-enumizer-runtime-api"
    const val COMPILER_PLUGIN_ID: String = "io.github.projectmapk.sealed-class-enumizer"
}
