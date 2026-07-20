package org.wrongwrong.diag.ambiguous

// TC-DIAG-020: 階層内の型が複数の末端 interface を実装 → ENUMIZE_AMBIGUOUS_KIND
class Amb2C : Amb2.LeafA, Amb2.LeafB
