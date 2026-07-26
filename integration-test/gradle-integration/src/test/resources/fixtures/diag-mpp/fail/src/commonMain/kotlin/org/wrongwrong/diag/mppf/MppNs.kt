package org.wrongwrong.diag.mppf

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-06: 非 sealed × expect への付与は NOT_SEALED 単独
// （checkBase の早期リターンで ON_EXPECT 不在 = 抑止範囲の固定）
@Enumize
expect interface MppNs
