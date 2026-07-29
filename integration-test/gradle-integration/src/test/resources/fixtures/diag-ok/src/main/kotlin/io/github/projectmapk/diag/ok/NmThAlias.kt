package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-52 用: 手動 supertype の頭（Enumized 自体）への typealias（先解決配置）
typealias NmThAlias = Enumized<NmThSi.Enumish>
