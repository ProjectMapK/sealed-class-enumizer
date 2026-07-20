package org.wrongwrong.sweep.fail

// TC-GAP-004(a): @Enumize 無しの sealed fun interface そのものの言語可否
// → 言語が拒否する（'sealed fun interface' is unsupported）ため @Enumize 診断には到達しない
sealed fun interface SwSfi {
    fun handle(): Int
}
