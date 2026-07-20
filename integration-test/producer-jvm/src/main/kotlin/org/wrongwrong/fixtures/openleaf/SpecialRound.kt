package org.wrongwrong.fixtures.openleaf

// 末端 Round のサブタイプである object（TC-LEAF-095）。「object = 自身が kind」は直接末端の場合のみで、
// 末端のサブタイプである object は末端の kind へ吸収される
object SpecialRound : Figure.Round(9)
