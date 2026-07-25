package org.wrongwrong.sweep.tahead

import org.wrongwrong.sealedClassEnumizer.Enumized

// 手動 supertype の頭（Enumized 自体）への typealias。型引数だけを別名にする TC-MAN-069 との対比
typealias SwThAlias = Enumized<SwThSi.Enumish>
