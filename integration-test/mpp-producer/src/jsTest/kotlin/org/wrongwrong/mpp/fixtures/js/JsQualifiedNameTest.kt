package org.wrongwrong.mpp.fixtures.js

// docs/test/ケース05-境界横断.md XMP-33 の負値半面: JS では KClass.qualifiedName が利用できない（docs/概要.md §2 が
// simpleName のみ使う理由）。実測: `SI.Foo::class.qualifiedName` の参照はコンパイルエラー
// 「This reflection API is not supported in Kotlin/JS.」になる（= 実行到達以前にコンパイルで不可）。
// コンパイル不能のためテストとしては保持できず、実測結果はこのコメントが記録する。
// simpleName 経路の正値は commonTest（SiApiSurfaceTest#goldenSnapshotMatchesOnEveryTarget）が担う
class JsQualifiedNameTest
