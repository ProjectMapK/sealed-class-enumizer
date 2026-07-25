package org.wrongwrong.diag.nm

// TC-DIAG-111: FQN 表記の supertype でも候補判定が働き、companion 自動生成により非発火
class NmFqFoo(val v: Int) : org.wrongwrong.diag.nm.NmAl
