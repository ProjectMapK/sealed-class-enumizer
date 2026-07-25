package org.wrongwrong.fixtures.widerleaf

// TC-GAP-014 生成側: 「internal 基底より広い public interface 末端」は言語上構成できない。
// 露出検査は interface の継承（extends 形）には働き、class が interface を実装する形には働かない
// （エッジ §1.1 #5 と #2 の対比。実測: Kotlin 2.4.20-Beta1）。
// 最小再現（コメントアウト解除でコンパイルエラー）:
//
//   interface Wide : InternalBase {   // e: 'public' sub-interface exposes its 'internal' supertype
// 'InternalBase'
//       companion object
//   }
//
// 基底より広い末端が成立するのは class 系末端（PublicLeaf.kt）のみ（エッジ §1.2）
