package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-34: 同一階層の参照不能末端（別ファイル private TL）。
// IR-only アクセサで load し、LABEL_CLASH を抑止も誘発もしない
private class Lc4Priv : Lc4Si
