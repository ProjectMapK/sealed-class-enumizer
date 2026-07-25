package org.wrongwrong.fixtures.widerleaf

import org.wrongwrong.sealedClassEnumizer.Enumize

// internal 基底（E-2 の生成側: 基底より広い可視性の末端は PublicLeaf.kt。docs/エッジケースへの対応方針.md §1.2）
@Enumize internal sealed interface InternalBase
