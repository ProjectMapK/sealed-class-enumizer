package org.wrongwrong.mpp.consumer

import org.wrongwrong.mpp.fixtures.SI

// プラグイン未適用モジュールの commonMain から生成 API を参照する利用関数
// （docs/テストケース管理.md mpp-consumer 行・TC-MPP-015/047 の跨モジュール面）。
// このファイル自体の metadata / platform コンパイルが「生成宣言が producer のメタデータに
// 直列化されている」ことの実証になる
fun consumerLabels(): List<String> = SI.Enumish.entries.map { it.label }

// NG 記録（TC-MPP-047 の跨モジュール面・docs/修正方針案.md 反映待ち）:
// else 無し kind-when の本来形は
//     fun consumerClassify(si: SI): String = when (si.asEnumish()) {
//         SI.Foo.Companion -> "foo"
//         SI.Bar -> "bar"
//     }
// だが、共通ソース由来の生成 companion（SI.Foo.Companion）が跨モジュールで
// Unresolved reference 'Companion' となり（同時に網羅性検査は 'Companion' 枝の欠落を報告 =
// inheritors 自体は直列化されている）、枝が書けないため定義できない
