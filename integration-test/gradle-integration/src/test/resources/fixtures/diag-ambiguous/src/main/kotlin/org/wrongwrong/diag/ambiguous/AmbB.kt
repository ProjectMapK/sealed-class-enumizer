package org.wrongwrong.diag.ambiguous

// TC-DIAG-019: 末端 AmbA を継承しつつ基底も直接実装 → ENUMIZE_AMBIGUOUS_KIND（2 つの kind に対応）
class AmbB : AmbA(), AmbSi
