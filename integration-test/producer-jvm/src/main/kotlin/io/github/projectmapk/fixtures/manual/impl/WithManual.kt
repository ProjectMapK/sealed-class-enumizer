package io.github.projectmapk.fixtures.manual.impl

import io.github.projectmapk.sealedClassEnumizer.Enumize
import kotlin.reflect.KClass

// 手動実装の許容フィクスチャ（docs/test/ケース01-生成と実行時API.md API-40/API-41）。
// 階層内による生成 Enumish の実装はエラーにならず、class 形の手動実装値は kind ではないため
// entries / valueOf に現れない。階層外からの直接実装は ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY
// （ケース04 が正典。自由実装の許容形は FreeAgent.kt）
@Enumize
sealed interface WithManual {
    data object Real : WithManual
}

// 階層内の手動実装（open 形。API-40）: 末端 class 自身が生成 Enumish を実装する形で、
// 直接実装として inheritors に登載される（kind 単位の網羅 when に is 枝が必要 = API-41）。
// インスタンスは Enumish として振る舞うが kind ではない（この末端の kind は自動生成 companion）
open class ManualLeaf(val v: Int) : WithManual, WithManual.Enumish {
    override val label: String
        get() = "manual-value"

    override val enumizedClass: KClass<out WithManual>
        get() = ManualLeaf::class
}

// open 手動実装の下流サブタイプ（API-41）。
// 生成 Enumish を直接実装しないため inheritors には登載されず、既存の is ManualLeaf 枝が被覆する
class ManualSub(v: Int) : ManualLeaf(v)

// 可視性の低い階層内手動実装（internal 形。API-40）。
// object 末端の生成 Enumish 冗長宣言は注入スキップで許容され（ケース04 DIA-51 の末端 Enumish
// 冗長宣言と同形）、kind は自身のまま entries に掲載される。メンバーの手動宣言は
// ENUMIZE_MEMBER_CONFLICT になるため置かない。internal のため跨 module の kind-when では
// else が必要になる（ケース05 が正典）
internal object ManualHidden : WithManual, WithManual.Enumish
