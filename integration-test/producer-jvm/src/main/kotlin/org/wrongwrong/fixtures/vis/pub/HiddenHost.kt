package org.wrongwrong.fixtures.vis.pub

// private 外側クラス内の末端（docs/test/ケース02-可視性.md VIS-18 = トップレベル壁:
// 外側チェーンにより基底スコープから名前参照できない配置でもアクセサ経由で entries に載る）
private class HiddenHost {
    object HostedLeaf : VisRoot
}
