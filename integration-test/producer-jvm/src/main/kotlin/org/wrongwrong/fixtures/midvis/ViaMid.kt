package org.wrongwrong.fixtures.midvis

import org.wrongwrong.sealedClassEnumizer.Enumize

// public 基底 + internal 中間 sealed + public 末端（TC-VIS-016）。
// 中間 sealed には何も生成されず、その可視性は生成 API に影響しない
@Enumize sealed interface ViaMid
