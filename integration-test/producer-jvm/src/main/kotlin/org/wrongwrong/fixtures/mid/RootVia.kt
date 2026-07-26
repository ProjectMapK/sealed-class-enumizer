package org.wrongwrong.fixtures.mid

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間 sealed 経由の raw 追跡再帰フィクスチャ（docs/test/ケース01-生成と実行時API.md
// API-29/API-30/API-52）。
// 継承者はファイル分散: MidClass.kt（sealed class 中間 + companion 末端）・
// MidIface.kt（sealed interface 中間 + 非実装 companion）と各経由の末端ファイル
@Enumize sealed interface RootVia
