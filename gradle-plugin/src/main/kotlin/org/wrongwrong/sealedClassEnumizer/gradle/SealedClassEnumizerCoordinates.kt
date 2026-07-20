package org.wrongwrong.sealedClassEnumizer.gradle

// 各値はビルドスクリプト（group / version / モジュール名 / gradlePlugin.id）と一致していなければならない
object SealedClassEnumizerCoordinates {
    const val GROUP: String = "org.wrongwrong"
    const val VERSION: String = "1.0-SNAPSHOT"
    const val COMPILER_PLUGIN_ARTIFACT: String = "compiler-plugin"
    const val RUNTIME_API_ARTIFACT: String = "runtime-api"
    const val COMPILER_PLUGIN_ID: String = "org.wrongwrong.sealed-class-enumizer"
}
