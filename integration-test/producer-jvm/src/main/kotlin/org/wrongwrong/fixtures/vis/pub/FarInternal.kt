package org.wrongwrong.fixtures.vis.pub

// 別ファイルの internal トップレベル末端（docs/test/ケース02-可視性.md VIS-16 の壁なし対照:
// internal は module 内可視のため基底スコープから直接参照できアクセサ不要）
internal class FarInternal(val v: Int) : VisRoot
