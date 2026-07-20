package org.wrongwrong.consumer.pure

import org.wrongwrong.fixtures.SI
import org.wrongwrong.fixtures.manualimpl.ManualLeaf
import org.wrongwrong.fixtures.manualimpl.WithManual
import org.wrongwrong.fixtures.zoo.Zoo
import kotlin.test.Test
import kotlin.test.assertEquals

// 跨モジュール網羅 when の正値実証（生成 Enumish の sealed inheritors がメタデータ直列化されている = V1-a/c/e。
// docs/概要.md §7・docs/テストケース管理.md TC-XM-008 / TC-XM-011 / TC-XM-012 / TC-LEAF-074 / TC-VIS-037 /
// TC-MAN-002）。いずれの when も else 無しでコンパイルできること自体が網羅性算入の検査である
class CrossModuleWhenTest {
    // TC-XM-008 / TC-VIS-037: 全 kind が public な階層では、プラグイン未適用の跨モジュール
    // kind-when が else 無しで成立する（V1-a）
    @Test
    fun kindWhenIsExhaustiveWithoutElseAcrossModule() {
        val si: SI = SI.Foo(1)
        val result = when (si.asEnumish()) {
            SI.Foo.Companion -> "foo"
            SI.Bar -> "bar"
        }
        assertEquals("foo", result)
    }

    // TC-XM-011: 値単位 when は sealed の地力（V1 非依存）で跨モジュールでも else 不要・スマートキャスト有効
    @Test
    fun valueWhenIsExhaustiveWithoutElseAcrossModule() {
        val values: List<SI> = listOf(SI.Foo(41), SI.Bar)
        val branches = values.map { si ->
            when (si) {
                is SI.Foo -> "foo:${si.v + 1}"
                SI.Bar -> "bar"
            }
        }
        assertEquals(listOf("foo:42", "bar"), branches)
    }

    // TC-LEAF-074: 全種別末端（data class / data object / object / open / abstract / interface /
    // fun interface / enum / value class）の kind を跨モジュールで名指しし、kind-when が else 無しで網羅する
    @Test
    fun allLeafShapeKindsAreCoveredWithoutElseAcrossModule() {
        val branches = Zoo.Enumish.entries.map { kind ->
            when (kind) {
                Zoo.AbstractLeaf.Companion -> "AbstractLeaf"
                Zoo.DataLeaf.Companion -> "DataLeaf"
                Zoo.EnumLeaf.Companion -> "EnumLeaf"
                Zoo.FunLeaf.Companion -> "FunLeaf"
                Zoo.IfaceLeaf.Companion -> "IfaceLeaf"
                Zoo.ObjectLeaf -> "ObjectLeaf"
                Zoo.OpenLeaf.Companion -> "OpenLeaf"
                Zoo.PlainObject -> "PlainObject"
                Zoo.ValueLeaf.Companion -> "ValueLeaf"
            }
        }
        assertEquals(
            listOf(
                "AbstractLeaf",
                "DataLeaf",
                "EnumLeaf",
                "FunLeaf",
                "IfaceLeaf",
                "ObjectLeaf",
                "OpenLeaf",
                "PlainObject",
                "ValueLeaf",
            ),
            branches,
        )
    }

    // TC-XM-012 / TC-MAN-002: 階層内手動実装（ManualLeaf）も生成 Enumish の inheritors に載り（V1-(e)）、
    // 跨モジュール kind-when の網羅には is ManualLeaf 枝が必要になる。手動実装の値は kind ではないため
    // entries には現れず、直接 when に通した場合のみこの枝へ到達する
    @Test
    fun manualImplementationBranchCountsAcrossModule() {
        val targets: List<WithManual.Enumish> = WithManual.Enumish.entries + ManualLeaf(1)
        val branches = targets.map { kind ->
            when (kind) {
                WithManual.Real -> "real"
                ManualLeaf.Companion -> "leaf-kind"
                is ManualLeaf -> "manual"
            }
        }
        assertEquals(listOf("leaf-kind", "real", "manual"), branches)
    }
}
