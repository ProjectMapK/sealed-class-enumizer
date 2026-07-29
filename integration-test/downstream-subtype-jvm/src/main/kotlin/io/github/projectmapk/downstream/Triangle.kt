package io.github.projectmapk.downstream

import io.github.projectmapk.fixtures.zoo.Zoo

// 非 final 末端（abstract class AbstractLeaf）の下流サブタイプ（docs/test/ケース05-境界横断.md XMP-18）。
// プラグイン未適用の別モジュールで定義しても新しい kind を作らず、asEnumish の実装を
// AbstractLeaf から継承して AbstractLeaf の kind に吸収される（V10）
class Triangle : Zoo.AbstractLeaf()
