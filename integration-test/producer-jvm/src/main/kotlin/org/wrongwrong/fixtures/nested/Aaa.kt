package org.wrongwrong.fixtures.nested

// Mid の末端（Mid の内側ではなくトップレベル）。NestedRoot の entries では
// Mid の位置に入れ子展開されるため、全体としては FQN 序数順にならない（[Bbb, Aaa]）
data object Aaa : Mid
