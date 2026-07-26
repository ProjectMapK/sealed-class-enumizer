package org.wrongwrong.fixtures.vis.pub

// internal 中間 HiddenMid 経由の public data object 末端（docs/test/ケース02-可視性.md VIS-21。
// 露出検査は interface 実装に働かないため、internal 中間より広い public 末端が言語上成立する
// = docs/エッジケースへの対応方針.md §1.1 #2 の成立形）
data object MA : HiddenMid
