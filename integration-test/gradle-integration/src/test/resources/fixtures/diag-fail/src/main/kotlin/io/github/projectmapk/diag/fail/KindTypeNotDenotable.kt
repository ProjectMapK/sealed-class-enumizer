package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-25: 規則 3（基底より広い末端 × 基底より狭い companion）
// → ENUMIZE_KIND_TYPE_NOT_DENOTABLE。internal companion 形と private companion 形の 2 亜種

@Enumize
internal sealed interface KtdSi

class KtdLeaf : KtdSi {
    internal companion object
}

@Enumize
internal sealed interface KnaxSi

// private companion 亜種（IR-only アクセサ解決後も規則 3 のみ残る）
class KnaxLeaf : KnaxSi {
    private companion object
}
