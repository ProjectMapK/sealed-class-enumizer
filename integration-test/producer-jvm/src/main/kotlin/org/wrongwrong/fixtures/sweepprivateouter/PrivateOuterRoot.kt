package org.wrongwrong.fixtures.sweepprivateouter

import org.wrongwrong.sealedClassEnumizer.Enumize

// private 外側クラスにネストした別ファイル末端の実挙動フィクスチャ（docs/テストケース管理.md
// TC-LEAF-090）。doc は ENUMIZE_KIND_NOT_ACCESSIBLE の発火を期待するが、実測は非発火で
// 生成が成立し実行時も正常（docs/修正方針案.md 反映待ちの新規 NG。本フィクスチャが実挙動を固定する）
@Enumize
sealed interface PrivateOuterRoot {
    data object Visible : PrivateOuterRoot
}
