package org.wrongwrong.widerfix

import org.wrongwrong.sealedClassEnumizer.Enumize

// internal 基底（sealed interface のため基底より広い可視性の末端が言語上成立する。エッジ §1）
@Enumize
internal sealed interface WSI
