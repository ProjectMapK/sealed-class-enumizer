package org.wrongwrong.fixtures.placement

import org.wrongwrong.sealedClassEnumizer.Enumize

// 全末端が同一パッケージのトップレベルにある構成（TC-ORD-002）。継承者は Aaa.kt / Bbb.kt / Ccc.kt
@Enumize
sealed interface Flat
