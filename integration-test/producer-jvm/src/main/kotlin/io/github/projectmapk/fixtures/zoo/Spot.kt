package io.github.projectmapk.fixtures.zoo

// 末端サブタイプの object（docs/test/ケース01-生成と実行時API.md API-13）。
// object であっても自 kind を作らず OpenLeaf の kind へ吸収される
object Spot : Zoo.OpenLeaf()
