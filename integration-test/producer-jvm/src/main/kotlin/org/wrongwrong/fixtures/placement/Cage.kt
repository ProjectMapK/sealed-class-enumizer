package org.wrongwrong.fixtures.placement

import org.wrongwrong.sealedClassEnumizer.Enumize

// 全末端が同一パッケージの別クラス内にネストする構成（TC-ORD-003）。末端は Crate.kt の内側
@Enumize sealed interface Cage
