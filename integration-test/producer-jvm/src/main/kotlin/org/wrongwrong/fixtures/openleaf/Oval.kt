package org.wrongwrong.fixtures.openleaf

// open class 末端の同一モジュール内サブタイプ（TC-LEAF-035）。新しい kind を作らず Round に吸収される
class Oval : Figure.Round(1)
