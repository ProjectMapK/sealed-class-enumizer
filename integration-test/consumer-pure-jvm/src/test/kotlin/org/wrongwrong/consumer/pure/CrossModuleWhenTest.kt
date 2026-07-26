package org.wrongwrong.consumer.pure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.wrongwrong.fixtures.manual.impl.ManualLeaf
import org.wrongwrong.fixtures.manual.impl.ManualSub
import org.wrongwrong.fixtures.manual.impl.WithManual
import org.wrongwrong.fixtures.si.SI
import org.wrongwrong.fixtures.zoo.Zoo

// 跨 module 網羅 when の正値実証（生成 Enumish の sealed inheritors がメタデータ直列化されている = V1-a。
// docs/test/ケース05-境界横断.md XMP-09 / XMP-11 / XMP-12）。
// いずれの when も else 無しでコンパイルできること自体が網羅性算入の検査である
class CrossModuleWhenTest {
    // docs/test/ケース05-境界横断.md XMP-09: 全 kind が public な階層では、プラグイン未適用の
    // 跨 module kind-when が else 無しで成立する（V1-a）
    @Test
    fun kindWhenIsExhaustiveWithoutElse() {
        val si: SI = SI.Foo(1)
        val result =
            when (si.asEnumish()) {
                SI.Foo.Companion -> "foo"
                SI.Bar -> "bar"
            }
        assertEquals("foo", result)
    }

    // docs/test/ケース05-境界横断.md XMP-09: 全種別末端 12（zoo）の kind を跨 module で名指しし、
    // kind-when が else 無しで網羅する（object 系 = 自身・他 = 自動生成 / 明示 companion）
    @Test
    fun allLeafShapeKindsAreCovered() {
        val branches =
            Zoo.Enumish.entries.map { kind ->
                when (kind) {
                    Zoo.AbstractLeaf.Companion -> "AbstractLeaf"
                    Zoo.DataLeaf.Companion -> "DataLeaf"
                    Zoo.EnumLeaf.Companion -> "EnumLeaf"
                    Zoo.FinalLeaf.Companion -> "FinalLeaf"
                    Zoo.FunAuto.Companion -> "FunAuto"
                    Zoo.FunLeaf.Companion -> "FunLeaf"
                    Zoo.Ghost.Companion -> "Ghost"
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
                "FinalLeaf",
                "FunAuto",
                "FunLeaf",
                "Ghost",
                "IfaceLeaf",
                "ObjectLeaf",
                "OpenLeaf",
                "PlainObject",
                "ValueLeaf",
            ),
            branches,
        )
    }

    // docs/test/ケース05-境界横断.md XMP-11: 値単位 when は sealed の地力（V1 非依存）で
    // 跨 module でも else 不要・スマートキャスト有効
    @Test
    fun valueWhenIsExhaustiveWithoutElse() {
        val values: List<SI> = listOf(SI.Foo(41), SI.Bar)
        val branches = values.map { si ->
            when (si) {
                is SI.Foo -> "foo:${si.v + 1}"
                SI.Bar -> "bar"
            }
        }
        assertEquals(listOf("foo:42", "bar"), branches)
    }

    // docs/test/ケース05-境界横断.md XMP-12: 階層内手動実装 ManualLeaf は inheritors に載り（V1-(e)）
    // is 枝が必要になる一方、open 手動実装の下流サブタイプ ManualSub は inheritors 非追加のまま
    // 既存の is ManualLeaf 枝が跨 module でも被覆する。class 形の手動実装値は entries / valueOf に現れない。
    // internal な階層内手動実装 ManualHidden は跨 module で名指しできないため else 枝が必須になる
    // （else 省略の言語エラー固定は XMP-13 = gradle-integration の CrossModuleNegativeTest）
    @Test
    fun manualImplementationBranchCounts() {
        val targets: List<WithManual.Enumish> =
            WithManual.Enumish.entries + listOf(ManualLeaf(1), ManualSub(2))
        val branches = targets.map { kind ->
            when (kind) {
                WithManual.Real -> "real"
                ManualLeaf.Companion -> "leaf-kind"
                is ManualLeaf -> "manual"
                else -> "hidden:${kind.label}"
            }
        }
        assertEquals(
            listOf("hidden:ManualHidden", "leaf-kind", "real", "manual", "manual"),
            branches,
        )
        assertNull(WithManual.Enumish.valueOfOrNull("manual-value"))
    }
}
