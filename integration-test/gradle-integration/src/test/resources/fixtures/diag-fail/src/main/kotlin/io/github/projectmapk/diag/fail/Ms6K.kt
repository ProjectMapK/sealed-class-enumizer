package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumish

// docs/test/ケース04-診断.md DIA-49 用の非適格 K（supertype 経路内に自階層型 LeafI を含む）
interface Ms6K : Enumish, Ms6Si.LeafI
