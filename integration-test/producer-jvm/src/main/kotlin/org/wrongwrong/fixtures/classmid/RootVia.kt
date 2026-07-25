package org.wrongwrong.fixtures.classmid

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間 sealed 経由の raw-ref 再帰追跡フィクスチャ（TC-LEAF-023 / TC-LEAF-079）。
// 継承者は MidClass.kt（sealed class）・MidIface.kt（sealed interface）と各末端ファイル
@Enumize sealed interface RootVia
