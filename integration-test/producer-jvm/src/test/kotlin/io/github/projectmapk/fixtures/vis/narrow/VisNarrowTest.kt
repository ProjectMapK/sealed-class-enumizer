package io.github.projectmapk.fixtures.vis.narrow

import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// internal 基底階層の box テスト（docs/test/ケース02-可視性.md VIS-02/VIS-06/VIS-10/VIS-11/VIS-19/VIS-22）
class VisNarrowTest {
    private data class NarrowApiSnapshot(
        val labels: List<String>,
        val roundTripsAll: Boolean,
        val missing: String?,
        val valueLabel: String,
    )

    private data class WiderLeafSnapshot(
        val label: String,
        val valueLabel: String,
        val leafClassMatches: Boolean,
    )

    // docs/test/ケース02-可視性.md VIS-02: internal 基底は module 内で全 API
    // （entries / valueOf / label / asEnumish）が完全動作する
    @Test
    fun internalBaseServesFullApiInsideModule() {
        val entries = NarrowBase.Enumish.entries
        val expected =
            NarrowApiSnapshot(
                labels = listOf("AutoWide", "Nested", "NarrowLeaf", "PublicLeaf"),
                roundTripsAll = true,
                missing = null,
                valueLabel = "NarrowLeaf",
            )
        val actual =
            NarrowApiSnapshot(
                labels = entries.map { it.label },
                roundTripsAll = entries.all { NarrowBase.Enumish.valueOf(it.label) === it },
                missing = NarrowBase.Enumish.valueOfOrNull("Nope")?.label,
                valueLabel = NarrowLeaf(1).label,
            )
        assertEquals(expected, actual)
    }

    // docs/test/ケース02-可視性.md VIS-06: internal 基底内の public 宣言ネスト末端は実効 internal
    // （min 連鎖）で E-2 に該当せず、eff(C) = eff(L) の規則 1（具体型で受けられること自体が検査）
    @Test
    fun nestedPublicLeafIsEffectivelyInternalOnRule1() {
        val kind: NarrowBase.Nested.Companion = NarrowBase.Nested(1).asEnumish()
        assertSame(NarrowBase.Nested.Companion, kind)
    }

    // docs/test/ケース02-可視性.md VIS-10: internal 末端 × internal companion の実効同値は規則 1 のまま
    // （規則 2 は eff(C) < eff(L) のみ）
    @Test
    fun allInternalLeafStaysOnRule1() {
        val kind: NarrowLeaf.Companion = NarrowLeaf(1).asEnumish()
        assertSame(NarrowLeaf.Companion, kind)
    }

    // docs/test/ケース02-可視性.md VIS-11: 自動生成 companion は実効可視性が末端へ追随し、
    // 基底より広い末端でも常に規則 1（露出診断も規則 3 も発火しない）
    @Test
    fun autoCompanionOfWiderLeafStaysOnRule1() {
        val kind: AutoWide.Companion = AutoWide(1).asEnumish()
        assertSame(AutoWide.Companion, kind)
        assertEquals("AutoWide", kind.label)
    }

    // docs/test/ケース02-可視性.md VIS-19: internal 基底 + public class 末端（明示 public companion）は
    // 言語成立（診断なし=このファイルのコンパイル成功が兼ねる）・規則 1・値/kind API は末端の可視範囲で動作
    @Test
    fun widerPublicLeafProvidesValueApiOnRule1() {
        val leaf = PublicLeaf(1)
        val kind: PublicLeaf.Companion = leaf.asEnumish()
        val expected =
            WiderLeafSnapshot(
                label = "PublicLeaf",
                valueLabel = "PublicLeaf",
                leafClassMatches = true,
            )
        val actual =
            WiderLeafSnapshot(
                label = kind.label,
                valueLabel = leaf.label,
                leafClassMatches = kind.enumizedClass == PublicLeaf::class,
            )
        assertEquals(expected, actual)
    }

    // docs/test/ケース02-可視性.md VIS-22: 全 kind 可視の位置（module 内）では kind-when の else を省略できる
    @Test
    fun kindWhenNeedsNoElseInsideModule() {
        val value: NarrowBase = NarrowLeaf(1)
        val result =
            when (value.asEnumish()) {
                NarrowBase.Nested.Companion -> "nested"
                NarrowLeaf.Companion -> "narrow"
                AutoWide.Companion -> "auto"
                PublicLeaf.Companion -> "public"
            }
        assertEquals("narrow", result)
    }
}
