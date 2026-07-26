package org.wrongwrong.downstream

import org.wrongwrong.fixtures.zoo.Zoo

// 非 final 末端（interface IfaceLeaf）の下流実装（docs/test/ケース05-境界横断.md XMP-18）。
// asEnumish の default 実装を継承して IfaceLeaf の kind（明示 public companion）に吸収される（V10）
class MyCustom : Zoo.IfaceLeaf
