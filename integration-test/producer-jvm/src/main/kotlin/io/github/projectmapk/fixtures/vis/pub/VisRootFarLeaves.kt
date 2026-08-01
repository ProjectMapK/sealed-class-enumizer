package io.github.projectmapk.fixtures.vis.pub

// 基底のファイル外配置が要請となる VisRoot 末端群（docs/test/ケース02-可視性.md VIS-16/VIS-18）。
// private は宣言ファイル内でのみ可視であり、本ファイルに置くこと自体が壁の成立条件である
// （基底と同一ファイルへ移すと壁が消え、VIS-18 のトップレベルアクセサ経路が観測できなくなる）。
// 生成されるトップレベルアクセサは kind の外側クラス連鎖を名前へ含むため、1 ファイルに複数の壁が
// 同居しても衝突しない

// 別ファイルの internal トップレベル末端（VIS-16 の壁なし対照:
// internal は module 内可視のため基底スコープから直接参照できアクセサ不要）
internal class FarInternal(val v: Int) : VisRoot

// 別ファイルの private トップレベル末端（VIS-18 = トップレベル壁:
// 基底スコープから名前参照できず、壁と同一ファイルのトップレベル internal アクセサ関数経由で掲載される）
private class FarSecret(val v: Int) : VisRoot

// private 外側クラス内の末端（VIS-18 = トップレベル壁:
// 外側チェーンにより基底スコープから名前参照できない配置でもアクセサ経由で entries に載る）
private class HiddenHost {
    object HostedLeaf : VisRoot
}
