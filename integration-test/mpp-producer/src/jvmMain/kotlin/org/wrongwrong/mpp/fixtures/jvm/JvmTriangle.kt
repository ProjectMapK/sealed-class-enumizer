package org.wrongwrong.mpp.fixtures.jvm

import org.wrongwrong.mpp.fixtures.shape.Shape

// 非 final 末端（commonMain の Shape.Polygon）のサブタイプを同一モジュールの platform ソースセットに
// 置く構成（docs/test/ケース05-境界横断.md XMP-42）。サブタイプは sealed 階層の継承者ではないため
// ソースセット制約（言語エラー）に掛からず（コンパイル成立自体がその実証）、Polygon の kind に吸収される
class JvmTriangle : Shape.Polygon()
