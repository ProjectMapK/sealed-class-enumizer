package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-04: CLASS 以外への付与は言語 WRONG_ANNOTATION_TARGET のみ・NOT_SEALED 不在
@Enumize
typealias NsAlias = String

@Enumize
fun nsFunction(): Int = 0

@Enumize
val nsProperty: Int = 0
