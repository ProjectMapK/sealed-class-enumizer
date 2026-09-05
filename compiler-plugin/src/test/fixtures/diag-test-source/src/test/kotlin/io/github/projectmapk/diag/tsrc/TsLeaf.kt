package io.github.projectmapk.diag.tsrc

// docs/test/ケース04-診断.md DIA-71: test compilation の末端が main の @Enumize 基底を継承
// → sealed の同一モジュール（コンパイル単位）制約の言語エラーへ委譲・プラグイン診断は不在
data object TsLeaf : TsBase
