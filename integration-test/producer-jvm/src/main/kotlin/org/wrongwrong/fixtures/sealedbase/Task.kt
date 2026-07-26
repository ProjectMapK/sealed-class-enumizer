package org.wrongwrong.fixtures.sealedbase

import org.wrongwrong.sealedClassEnumizer.Enumize

// sealed class 基底の合成階層（docs/test/ケース01-生成と実行時API.md API-27/API-28/API-52・
// docs/test/ケース02-可視性.md VIS-09）。
// 構築子呼び出し形 `:Task()` の supertype からも companion 自動生成（V3）が成立する
@Enumize
sealed class Task {
    // companion 明示なし・構築子呼び出し supertype（API-27）
    data class Run(val v: Int) : Task()

    // data object 末端（toString は言語合成のまま）
    data object Done : Task()

    // 非 data object 末端（toString = label が生成される）
    object Plain : Task()

    // protected ネスト末端 + 明示 companion。実効可視性が末端と同等のため規則 1（具体型）で、
    // 基底スコープ内の直接参照により entries へ掲載される（VIS-09）
    protected class Inner : Task() {
        companion object
    }

    // 基底自身の companion が末端（API-52。COMPANION_LEAF_CONFLICT は外側 = 末端のみ検査）。
    // kind = 自身・label = 宣言名 "Companion"
    companion object : Task() {
        fun makeInner(): Task = Inner()

        // 規則 1 の基底スコープ内観測（VIS-09）: asEnumish は具体型 Inner.Companion で受けられる。
        // protected 型は公開できないため、観測後に公開型 Task.Enumish へ広げて保持する
        val innerKind: Task.Enumish = run {
            val kind: Inner.Companion = Inner().asEnumish()
            kind
        }
    }
}
