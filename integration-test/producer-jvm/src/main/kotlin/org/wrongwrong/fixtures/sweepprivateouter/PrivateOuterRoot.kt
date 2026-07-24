package org.wrongwrong.fixtures.sweepprivateouter

import org.wrongwrong.sealedClassEnumizer.Enumize

// private 外側クラスにネストした別ファイル末端の実挙動フィクスチャ（docs/テストケース管理.md
// TC-LEAF-090・#13 の解決形）。Leaf は基底本体スコープから名指し不可だが、末端ファイルの
// トップレベル IR-only アクセサ経由で entries に載る（概要 §8・設計02 §4.3）。本フィクスチャが実挙動を固定する
@Enumize
sealed interface PrivateOuterRoot {
    data object Visible : PrivateOuterRoot
}
