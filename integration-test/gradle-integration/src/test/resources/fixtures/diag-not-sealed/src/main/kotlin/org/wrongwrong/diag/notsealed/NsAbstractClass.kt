package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-004: @Enumize を abstract class に付与 → ENUMIZE_NOT_SEALED（abstract でも sealed でなければ発火）
@Enumize
abstract class NsAbstractClass
