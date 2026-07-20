package org.wrongwrong.downstream

import org.wrongwrong.fixtures.shape.Shape

// 非 final 末端（interface Custom）の下流実装（docs/テストケース管理.md TC-XM-017）。
// asEnumish の default 実装を継承して Custom の kind に吸収される（V10-b）
class MyCustom : Shape.Custom
