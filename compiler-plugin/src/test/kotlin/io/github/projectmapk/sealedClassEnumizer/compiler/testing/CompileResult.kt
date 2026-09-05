package io.github.projectmapk.sealedClassEnumizer.compiler.testing

import java.nio.file.Path
import org.jetbrains.kotlin.cli.common.ExitCode

// 1 コンパイル単位の結果。output は CLI が出す診断テキストそのもので、
// 形式（`e: file:///<パス>:<行>:<列> <メッセージ>`）は Gradle 経由の出力と同一であり、
// DiagAsserts はこの形式だけを前提とする
data class CompileResult(val exitCode: ExitCode, val output: String, val classesDir: Path) {
    val succeeded: Boolean = exitCode == ExitCode.OK
}
