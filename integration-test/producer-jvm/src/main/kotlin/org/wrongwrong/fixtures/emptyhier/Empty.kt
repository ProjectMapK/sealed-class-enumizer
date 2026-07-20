package org.wrongwrong.fixtures.emptyhier

import org.wrongwrong.sealedClassEnumizer.Enumize

// 継承者ゼロの空階層（TC-LEAF-099 / TC-ORD-061 / TC-BOX-066）。診断は発火しない
@Enumize
sealed interface Empty
