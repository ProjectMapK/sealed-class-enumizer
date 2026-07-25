package org.wrongwrong.fixtures.deepnested

import org.wrongwrong.sealedClassEnumizer.Enumize

// 多段中間 sealed の再帰展開フィクスチャ（TC-LEAF-055 / TC-LEAF-056 / TC-ORD-016 / TC-BOX-008）。
// Deep → DeepMid1 → DeepMid2 の 2 段の中間を経て末端 DeepA へ降りる（継承者はファイル分散）
@Enumize sealed interface Deep
