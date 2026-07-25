package org.wrongwrong.fixtures.companionleaf

import org.wrongwrong.sealedClassEnumizer.Enumize

// 階層外クラスの companion が単独で末端になる許容構成（TC-LEAF-046 / TC-ORD-057 / TC-BOX-023）。
// 継承者はファイル分散: Host.kt（既定名 companion 末端）・WithNamed.kt（名前つき companion 末端）・
// HostA.kt / Aaa.kt（順序境界の対照）
@Enumize sealed interface Token
