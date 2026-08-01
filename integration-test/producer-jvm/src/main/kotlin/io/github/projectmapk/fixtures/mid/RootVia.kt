package io.github.projectmapk.fixtures.mid

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 非注釈 sealed の祖先（階層の上端）。所属判定は sealed 連鎖を上へ辿るが、
// @Enumize 基底へ達しない側は階層に数えない（API-30 の探索境界の観測点）
sealed interface ViaAncestor

// 中間 sealed 経由の raw 追跡再帰フィクスチャ（docs/test/ケース01-生成と実行時API.md
// API-29/API-30/API-52）。中間は entries に載らず、経由末端の companion は自動生成される
@Enumize sealed interface RootVia : ViaAncestor

// 中間 sealed class（API-30 の経由点）。
// companion は自身が RootVia を実装する末端であり成立する（API-52。中間 companion 末端の成立形）
sealed class MidClass : RootVia {
    companion object : RootVia
}

// 中間 sealed interface（API-30 の経由点）。
// 明示 companion は階層を実装せず、Enumish 非注入・kind 非成立のまま残る（API-30 の観測点）
sealed interface MidIface : RootVia {
    companion object
}

// 中間 sealed class 経由の末端・companion 明示なし（API-29）。
// raw 追跡再帰が `:MidClass()` → MidClass → RootVia へ到達し companion が自動生成される
class LeafViaMid(val v: Int) : MidClass()

// 中間 sealed interface 経由の末端・companion 明示なし（API-29）
class LeafViaIface(val v: Int) : MidIface

// 祖先直下の非所属メンバー（RootVia の探索は基底から下だけを見るため kind を持たず entries にも載らない）
data object ViaOutside : ViaAncestor
