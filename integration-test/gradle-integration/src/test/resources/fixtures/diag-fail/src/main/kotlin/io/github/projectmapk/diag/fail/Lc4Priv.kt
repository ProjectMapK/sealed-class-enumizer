package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-34: Lc4Si 階層の参照不能末端（別ファイルの private トップレベル）。
// IR-only アクセサで load し、LABEL_CLASH を抑止も誘発もしない。
// private トップレベルはファイルスコープであり、基底と別ファイルであること自体が参照不能の条件である
private class Lc4Priv : Lc4Si
