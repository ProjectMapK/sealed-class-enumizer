package org.wrongwrong.fixtures.vis.pub

// 別ファイルの private トップレベル末端（docs/test/ケース02-可視性.md VIS-18 = トップレベル壁:
// 基底スコープから名前参照できず、壁と同一ファイルのトップレベル internal アクセサ関数経由で掲載される）
private class FarSecret(val v: Int) : VisRoot
