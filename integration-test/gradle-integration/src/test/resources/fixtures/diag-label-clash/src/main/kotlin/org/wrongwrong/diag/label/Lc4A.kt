package org.wrongwrong.diag.label

// TC-DIAG-103: LABEL_CLASH の当事者側（同一階層の参照不能末端 Lc4Priv と共存しても相互抑止しない）
class Lc4A {
    object Same : Lc4Si
}
