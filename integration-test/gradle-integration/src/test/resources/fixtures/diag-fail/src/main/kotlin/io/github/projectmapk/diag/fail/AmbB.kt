package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-17: 末端 AmbA を継承しつつ基底も直接実装 → ENUMIZE_AMBIGUOUS_KIND
class AmbB : AmbA(), AmbSi
