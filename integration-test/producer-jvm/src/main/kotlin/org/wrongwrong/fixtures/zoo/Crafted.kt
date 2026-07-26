package org.wrongwrong.fixtures.zoo

// interface 末端の第三者実装（docs/test/ケース01-生成と実行時API.md API-13）。
// default 実装の asEnumish（JVM lowering 込み）を継承し IfaceLeaf の kind へ吸収される
class Crafted : Zoo.IfaceLeaf
