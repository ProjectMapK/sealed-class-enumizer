package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-MAN-065 用: asEnumish の default 実装を持つ階層外ユーザー interface（型引数は生成 Enumish と一致）
interface SwMhManual : Enumized<SwMhSi.Enumish> {
    override fun asEnumish(): SwMhSi.Enumish = SwMhSi.Real
}
