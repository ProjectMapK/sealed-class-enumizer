package io.github.projectmapk.fixtures.vis.priv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

// private 基底階層の box テスト（docs/test/ケース02-可視性.md VIS-01/VIS-10/VIS-20/VIS-22。
// private 基底の可視範囲内の観測は PrivBase.kt の internal 観測関数を経由する）
class VisPrivTest {
    private data class PrivApiSnapshot(
        val labels: List<String>,
        val resolved: String?,
        val missing: String?,
        val valueLabels: List<String>,
    )

    private data class WiderLeafSnapshot(
        val label: String,
        val leafClassMatches: Boolean,
        val kindToString: String,
    )

    // docs/test/ケース02-可視性.md VIS-01: private 基底はファイル内で全 API
    // （entries / valueOf / label / asEnumish）が完全動作する
    @Test
    fun privateBaseServesFullApiInsideFile() {
        val expected =
            PrivApiSnapshot(
                labels = listOf("Datum", "Hidden", "Kept", "Wide"),
                resolved = "Hidden",
                missing = null,
                valueLabels = listOf("Hidden", "Datum", "Kept", "Wide"),
            )
        val actual =
            PrivApiSnapshot(
                labels = observePrivBaseLabels(),
                resolved = observePrivBaseValueOf("Hidden"),
                missing = observePrivBaseValueOf("Nope"),
                valueLabels = observePrivBaseValueLabels(),
            )
        assertEquals(expected, actual)
    }

    // docs/test/ケース02-可視性.md VIS-10: private 基底内の internal companion は実効 private 同士で規則 1
    @Test
    fun internalCompanionInsidePrivateBaseStaysOnRule1() {
        assertTrue(keptStaysOnRule1())
    }

    // docs/test/ケース02-可視性.md VIS-20: private 基底より広い public 末端の値/kind API は
    // 別ファイル（本テストファイル）から規則 1 で利用できる。
    // 階層 API（entries / Enumish 名指し）は基底可視性に従いこの位置では参照できない
    @Test
    fun widerLeafOfPrivateBaseWorksFromAnotherFile() {
        val kind: Wide.Companion = Wide(1).asEnumish()
        assertSame(Wide.Companion, kind)
        val expected =
            WiderLeafSnapshot(label = "Wide", leafClassMatches = true, kindToString = "Wide")
        val actual =
            WiderLeafSnapshot(
                label = kind.label,
                leafClassMatches = kind.enumizedClass == Wide::class,
                kindToString = kind.toString(),
            )
        assertEquals(expected, actual)
    }

    // docs/test/ケース02-可視性.md VIS-22: 全 kind 可視の位置（private 基底=同一ファイル内）では
    // kind-when の else を省略できる
    @Test
    fun kindWhenNeedsNoElseInsideFile() {
        assertEquals(listOf("hidden", "datum", "kept", "wide"), (0..3).map { pickPrivBase(it) })
    }
}
