package org.wrongwrong.fixtures.zoo

// 多段サブタイプ（docs/test/ケース01-生成と実行時API.md API-13/API-14）。
// OpenLeaf ← Quad ← Square の 2 段でも最上位末端 OpenLeaf の kind へ吸収される
class Square : Quad()
