package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-086: CLASS 以外への付与は言語の WRONG_ANNOTATION_TARGET でプラグイン診断へ到達しない
@Enumize
typealias NsAlias = String

@Enumize
fun nsFunction(): Int = 0

@Enumize
val nsProperty: Int = 0
