package io.github.projectmapk.fixtures.companionleaf

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 階層外クラスの companion が単独で末端になる許容構成（docs/test/ケース01-生成と実行時API.md
// API-25/API-26・docs/test/ケース03-順序.md ORD-08）。
// 継承者はファイル分散: Host.kt（既定名 companion 末端）・WithNamed.kt（名前つき companion 末端）・
// HostA.kt（'.'(46) 序数境界の対照）・Holder2.kt（名前つき companion の順序プローブ）。
// 基底 / 中間の companion 末端の成立形は sealedbase / mid が担う
@Enumize sealed interface Token
