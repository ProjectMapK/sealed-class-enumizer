package io.github.projectmapk.mpp.fixtures

import io.github.projectmapk.sealedClassEnumizer.Enumish

// commonMain から生成 API を参照する共通関数群。metadata コンパイルで解決できること（V5）と、
// 各 platform の IR 充填で同一結果になることを commonTest から観測する（docs/test/ケース05-境界横断.md XMP-39）
fun pickLabels(): List<String> = SI.Enumish.entries.map { it.label }

// else 無し kind-when（XMP-39: 生成 Enumish の inheritors が common metadata へ直列化され、
// commonMain のコードでも網羅が成立する = V1 × V5）
fun classify(si: SI): String =
    when (si.asEnumish()) {
        SI.Foo.Companion -> "foo"
        SI.Bar -> "bar"
    }

// enumishCompanion 経由の共通参照（XMP-39: metadata にはシグネチャのみが載り、
// ボディは各 platform の IR 充填で埋まる）
fun entriesVia(si: SI): List<SI.Enumish> = si.asEnumish().enumishCompanion.entries

// reified ヘルパ相当を利用者が自作しても intrinsic 書き換えは行われない
// （docs/test/ケース05-境界横断.md XMP-49）。
// プラグインが呼び出し点を Companion 参照へ書き換えるなら TODO() に到達しない
inline fun <reified T : Enumish> unresolvedHelper(): List<T> = TODO("no intrinsic rewriting")
