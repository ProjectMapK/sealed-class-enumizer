package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-17: 1 宣言が 2 つの kind に属する形 → ENUMIZE_AMBIGUOUS_KIND + 末端 FQN

@Enumize
sealed interface AmbSi

// 非 final 末端（V10）
abstract class AmbA : AmbSi

// 末端 AmbA を継承しつつ基底も直接実装
class AmbB : AmbA(), AmbSi

@Enumize
sealed interface Amb2 {
    interface LeafA : Amb2

    interface LeafB : Amb2
}

// 2 末端 interface の実装 → AK + 末端 FQN 2 件
class Amb2C : Amb2.LeafA, Amb2.LeafB
