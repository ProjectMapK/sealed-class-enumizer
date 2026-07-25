package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumish

// commonMain から生成 API を参照する共通関数群。metadata コンパイルで解決できること（V5）と、
// 各 platform の IR 充填で同一結果になることを commonTest から観測する（docs/テストケース管理.md TC-MPP-018）
fun pickLabels(): List<String> = SI.Enumish.entries.map { it.label }

// else 無し kind-when（TC-MPP-047: 生成 Enumish の inheritors が common metadata へ直列化され、
// commonMain のコードでも網羅が成立する = V1 × V5）
fun classify(si: SI): String =
    when (si.asEnumish()) {
        SI.Foo.Companion -> "foo"
        SI.Bar -> "bar"
    }

// enumishCompanion 経由の共通参照（TC-MPP-066: metadata にはシグネチャのみが載り、
// ボディは各 platform の IR 充填で埋まる）
fun entriesVia(si: SI): List<SI.Enumish> = si.asEnumish().enumishCompanion.entries

// reified ヘルパ相当を利用者が自作しても intrinsic 書き換えは行われない（TC-MPP-038）。
// プラグインが呼び出し点を Companion 参照へ書き換えるなら TODO() に到達しない
inline fun <reified T : Enumish> unresolvedHelper(): List<T> = TODO("no intrinsic rewriting")
