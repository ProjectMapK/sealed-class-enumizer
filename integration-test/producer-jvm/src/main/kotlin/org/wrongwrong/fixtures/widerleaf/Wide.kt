package org.wrongwrong.fixtures.widerleaf

// NG（TC-GAP-014 生成側）: 「internal 基底より広い public interface 末端」は言語上構成できない。
// エッジ §1.1 #2 の「露出検査はスーパークラスにしか働かない」は class が interface を実装する場合の実測であり、
// interface が interface を継承する場合には露出検査が働く（実測: Kotlin 2.4.20-Beta1）。
// 最小再現（コメントアウト解除でコンパイルエラー）:
//
//   interface Wide : InternalBase {   // e: 'public' sub-interface exposes its 'internal' supertype 'InternalBase'
//       companion object
//   }
//
// 基底より広い末端が成立するのは class 末端（PublicLeaf.kt）のみ。docs/修正方針案.md 反映待ち
