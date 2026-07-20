package org.wrongwrong.diag.cr

import org.wrongwrong.diag.cr.CrSi as AliasedBase

// TC-DIAG-058: import エイリアス経由 supertype の末端 class に companion 無し → ENUMIZE_COMPANION_REQUIRED
class CrBar(val v: Int) : AliasedBase
