package org.wrongwrong.mpp.consumer

import org.wrongwrong.mpp.fixtures.SI

// プラグイン未適用モジュールの commonMain から生成 API を参照する利用関数
// （docs/test/フィクスチャ構成.md §2 mpp-consumer 行・docs/test/ケース05-境界横断.md XMP-37）。
// このファイル自体の metadata / platform コンパイルが「生成宣言が producer のメタデータに
// 直列化されている」ことの実証になる
fun consumerLabels(): List<String> = SI.Enumish.entries.map { it.label }

// else 無し kind-when（docs/test/ケース05-境界横断.md XMP-38）。共通ソース由来の生成 companion
// （SI.Foo.Companion）を跨モジュールで名指しでき、網羅性検査も else なしで満たされる
fun consumerClassify(si: SI): String =
    when (si.asEnumish()) {
        SI.Foo.Companion -> "foo"
        SI.Bar -> "bar"
    }
