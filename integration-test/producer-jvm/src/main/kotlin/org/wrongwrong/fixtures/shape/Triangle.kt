package org.wrongwrong.fixtures.shape

// 非 final 末端（abstract class）のサブタイプ。新しい kind を作らず Polygon の kind に吸収される
class Triangle : Shape.Polygon()
