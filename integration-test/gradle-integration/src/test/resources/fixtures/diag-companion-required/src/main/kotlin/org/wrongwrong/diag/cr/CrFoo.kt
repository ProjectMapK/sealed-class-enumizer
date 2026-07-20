package org.wrongwrong.diag.cr

// TC-DIAG-057: typealias 経由 supertype の末端 class に companion 無し → ENUMIZE_COMPANION_REQUIRED
class CrFoo(val v: Int) : CrAlias
