package io.github.projectmapk.diag.fail.visibilityscope

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-28: 可視性スコープ違反は言語エラーのみ（プラグイン診断は不在）。
// private / protected はクラススコープであり、同一ファイルでも外側クラスの外からは参照できない。
// private トップレベル基底はファイルスコープのため ScopePriv.kt / ScopePrivUse.kt が別に担う

// private ネスト基底（外側クラス本体内では全 API 成立）
class PrivHost {
    @Enumize
    private sealed interface N {
        data object L : N
    }

    fun insideCount(): Int = N.Enumish.entries.size
}

// 外側クラスの外からの参照 → 言語可視性エラーのみ
fun privHostUse(x: PrivHost.N): String = x.toString()

// protected ネスト基底（サブクラス文脈の成立側は XMP-50）
open class ProtHost {
    @Enumize
    protected sealed interface P {
        data object L : P
    }
}

// 非サブクラス位置からの参照 → 言語可視性エラーのみ
fun protHostUse(x: ProtHost.P): String = x.toString()
