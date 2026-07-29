package io.github.projectmapk.widerfix

import io.github.projectmapk.sealedClassEnumizer.Enumize

// internal 基底（sealed interface のため基底より広い可視性の末端が言語上成立する。docs/エッジケースへの対応方針.md §1）
@Enumize
internal sealed interface WSI
