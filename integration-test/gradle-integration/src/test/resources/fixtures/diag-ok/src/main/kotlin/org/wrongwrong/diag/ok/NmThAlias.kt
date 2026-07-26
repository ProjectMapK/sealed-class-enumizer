package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-52 用: 手動 supertype の頭（Enumized 自体）への typealias（先解決配置）
typealias NmThAlias = Enumized<NmThSi.Enumish>
