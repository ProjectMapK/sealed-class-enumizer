package org.wrongwrong.mpp.fixtures.jvm

import org.wrongwrong.mpp.fixtures.shape.Shape

// 非 final 末端（commonMain の Shape.Polygon）のサブタイプを同一モジュールの platform ソースセットに
// 置く構成（docs/テストケース管理.md TC-MPP-050）。サブタイプは sealed 階層の継承者ではないため
// ENUMIZE_CROSS_SOURCE_SET は発火せず（コンパイル成立自体が非発火の実証）、Polygon の kind に吸収される
class JvmTriangle : Shape.Polygon()
