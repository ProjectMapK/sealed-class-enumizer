package io.github.projectmapk.fixtures.zoo

// 末端 interface への委譲実装（docs/test/ケース01-生成と実行時API.md API-13）。
// kind を新設せず、委譲された asEnumish が IfaceLeaf の kind を返す。
// 基底 Zoo への直接委譲は診断対象（docs/test/ケース04-診断.md DIA-69）であり、ここでは末端への委譲のみ扱う
class Veil(impl: Zoo.IfaceLeaf) : Zoo.IfaceLeaf by impl
