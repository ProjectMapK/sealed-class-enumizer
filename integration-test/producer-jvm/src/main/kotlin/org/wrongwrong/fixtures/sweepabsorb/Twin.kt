package org.wrongwrong.fixtures.sweepabsorb

// TC-MAN-079: 末端 SweepRoot.Twin と同じ単純名を持つ階層外サブタイプ（kind でなく label を持たない
// ため衝突判定の対象外。Wide の kind に吸収される）
class Twin : SweepRoot.Wide()
