package org.wrongwrong.diag.label

// TC-DIAG-103: 同一階層内の参照不能な末端（別ファイルの private トップレベル）。IR-only アクセサで load し
// 診断を出さない = LABEL_CLASH を抑止も誘発もしない
private class Lc4Priv : Lc4Si
