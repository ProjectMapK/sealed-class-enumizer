package org.wrongwrong.gradle

import kotlin.test.assertTrue

// ビルド出力に対する診断アサーション。K2 の診断行は
// 「e: file:///...<ファイル名>:<行>:<列> <メッセージ>」形式（警告は w:）で 1 診断 = 1 行に出る。
// 位置は「宣言の先頭行（アノテーション含む）」または「メンバー・supertype 参照の行」（docs/コンパイラプラグイン設計01.md §7.1 の報告位置）。
object DiagAsserts {
    // 指定ファイル:行 の診断行に、指定断片がすべて含まれることを検査する
    fun assertDiagnosticAt(output: String, file: String, line: Int, vararg fragments: String) {
        val positioned = linesAt(output, file, line)
        assertTrue(
            positioned.any { candidate -> fragments.all(candidate::contains) },
            "期待した診断が $file:$line に無い: ${fragments.toList()}\n" +
                "位置一致行: $positioned\n診断行一覧:\n${diagnosticSummary(output)}",
        )
    }

    // 位置を問わず、指定断片をすべて含む出力行があることを検査する（ICE のように診断の座標を
    // 持たない出力や、報告位置を検証対象にしないケースで発火の事実だけを見る用途）
    fun assertDiagnosticAnywhere(output: String, vararg fragments: String) {
        val hit = output.lineSequence().any { candidate -> fragments.all(candidate::contains) }
        assertTrue(hit, "期待した診断が出力に無い: ${fragments.toList()}\n診断行一覧:\n${diagnosticSummary(output)}")
    }

    // 指定ファイル:行 に診断（e:/w:）が 1 件も出ていないことを検査する
    fun assertNoDiagnosticAt(output: String, file: String, line: Int) {
        val positioned = linesAt(output, file, line).filter(::isDiagnosticLine)
        assertTrue(positioned.isEmpty(), "診断が $file:$line に出ている: $positioned")
    }

    // 指定ファイルの診断行（e:/w:）に、指定断片をすべて含む行があることを検査する（行番号は問わない。
    // 言語診断のように報告行がプラグイン管轄外のケース用）
    fun assertDiagnosticInFile(output: String, file: String, vararg fragments: String) {
        val lines =
            output.lineSequence().filter { it.contains("$file:") && isDiagnosticLine(it) }.toList()
        assertTrue(
            lines.any { candidate -> fragments.all(candidate::contains) },
            "期待した診断が $file に無い: ${fragments.toList()}\n$file の診断行: $lines",
        )
    }

    // 出力全体に指定断片が現れないことを検査する（診断の非発火確認）
    fun assertFragmentAbsent(output: String, fragment: String) {
        val hits = output.lineSequence().filter { it.contains(fragment) }.toList()
        assertTrue(hits.isEmpty(), "出力に現れないはずの断片 \"$fragment\" が存在する: $hits")
    }

    // 指定ファイルに関する行に指定断片が現れないことを検査する（他ファイルの発火と共存する場合の限定版）
    fun assertFragmentAbsentAt(output: String, file: String, fragment: String) {
        val hits =
            output.lineSequence().filter { it.contains(file) && it.contains(fragment) }.toList()
        assertTrue(hits.isEmpty(), "$file の行に現れないはずの断片 \"$fragment\" が存在する: $hits")
    }

    private fun linesAt(output: String, file: String, line: Int): List<String> =
        output.lineSequence().filter { it.contains("$file:$line:") }.toList()

    private fun isDiagnosticLine(line: String): Boolean =
        line.contains("e: ") || line.contains("w: ")

    private fun diagnosticSummary(output: String): String =
        output.lineSequence().filter(::isDiagnosticLine).joinToString(separator = "\n").take(4000)
}
