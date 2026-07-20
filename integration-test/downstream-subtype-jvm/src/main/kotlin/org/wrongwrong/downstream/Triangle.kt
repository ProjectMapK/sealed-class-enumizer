package org.wrongwrong.downstream

import org.wrongwrong.fixtures.shape.Shape

// 非 final 末端（abstract class Polygon）の下流サブタイプ（docs/概要.md §3「末端は非 final でもよい」・
// docs/テストケース管理.md TC-XM-016）。プラグイン未適用の別モジュールで定義しても新しい kind を作らず、
// asEnumish の実装を Polygon から継承して Polygon の kind に吸収される
class Triangle : Shape.Polygon()
