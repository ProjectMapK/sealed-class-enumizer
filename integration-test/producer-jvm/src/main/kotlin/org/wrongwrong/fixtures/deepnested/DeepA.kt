package org.wrongwrong.fixtures.deepnested

// 2 段の中間を経た末端。DeepMid1 の継承者一覧 [DeepB, DeepMid2] の DeepMid2 位置に展開される
data object DeepA : DeepMid2
