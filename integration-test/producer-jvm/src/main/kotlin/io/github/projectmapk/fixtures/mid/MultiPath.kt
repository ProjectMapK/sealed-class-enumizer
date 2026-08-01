package io.github.projectmapk.fixtures.mid

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 合流（複数経路で到達するメンバー）の合成階層（docs/test/ケース01-生成と実行時API.md API-57）。
// 合流点の種別（非 sealed 末端 / 中間 sealed / companion 末端）× 経路の非対称性
// （対称 / 基底直下併存 / sealed class 中間との混成）× 入れ子を 1 階層へ合成し、
// entries スナップショットで初出 1 回掲載を固定する（展開順の規則は docs/test/ケース03-順序.md §1）。
// RootVia とは独立した階層とし、entries スナップショットを相互に汚さない
@Enumize sealed interface MultiPath

// 兄弟中間の一方（合流するメンバーの初出経路）
sealed interface MpLeft : MultiPath

// 兄弟中間の他方（合流するメンバーの 2 度目の到達経路）
sealed interface MpRight : MultiPath

// sealed class の中間（混成合流の class 側経路）
sealed class MpClassMid : MultiPath

// 合流点が中間 sealed の形。MpLeft 経由で 1 度だけ展開され、
// MpRight からの再到達では配下のサブツリーごと再走査されない
sealed interface MpShared : MpLeft, MpRight

// 合流点 = 非 sealed 末端・対称経路（兄弟中間の同時実装。companion 明示なし = 多重経路での候補判定も観測する）
class MpBoth : MpLeft, MpRight

// 合流点 = companion 末端（外側 MpHost は階層外）。kind = companion 自身・label = 宣言名
class MpHost {
    companion object : MpLeft, MpRight
}

// 経路の非対称（基底直下 + 中間経由）。基底の継承者一覧で MpRight より先に来るため基底直下の位置に載る
data object MpDirect : MultiPath, MpRight

// 経路の混成（sealed class 中間 + sealed interface 中間）
class MpMixed : MpClassMid(), MpRight

// 合流の入れ子（合流点 MpShared の配下でさらに MpLeft 直下の経路と交差する）
class MpNested : MpShared, MpLeft

// 合流点 MpShared の配下末端（サブツリーが 1 度だけ展開されることの観測点）
data object MpDeep : MpShared

// MpLeft 単独経路の末端（合流するメンバーの後続 = 初出位置の対照）
data object MpOnlyLeft : MpLeft

// MpRight 単独経路の末端（合流の重複が除かれた後に続く）
data object MpOnlyRight : MpRight
