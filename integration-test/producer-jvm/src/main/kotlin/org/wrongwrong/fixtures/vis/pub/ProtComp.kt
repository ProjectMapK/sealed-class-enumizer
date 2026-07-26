package org.wrongwrong.fixtures.vis.pub

// トップレベル末端 × protected companion（docs/test/ケース02-可視性.md VIS-17 = ネスト壁。
// protected companion は class 内でのみ言語上構成できる。非 final 末端 × private companion の
// 組はケース05 の mpp kindaccessor が担う）
class ProtComp(val v: Int) : VisRoot {
    protected companion object
}
