package org.wrongwrong.fixtures.scope.other

import org.wrongwrong.sealedClassEnumizer.Enumize

// 別 pkg の実基底（docs/test/ケース01-生成と実行時API.md API-51 競合形 (2) の対向）。
// 継承者ゼロ: sealed の同一パッケージ制約により scope.target 側の末端は言語上ここへ所属できない
@Enumize sealed interface Base
