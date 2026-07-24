package org.wrongwrong.diag.label

// TC-DIAG-103: LABEL_CLASH 側の逸脱（load する参照不能末端 Lc4Priv と同一階層で共存し相互抑止しない）
class Lc4A {
    object Same : Lc4Si
}
