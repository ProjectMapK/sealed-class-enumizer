package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// FQN が接頭辞関係にある 2 末端（TC-ORD-049）: 短い方（Foo）が先行する。末端は Foo.kt / FooBar.kt
@Enumize sealed interface Pfx
