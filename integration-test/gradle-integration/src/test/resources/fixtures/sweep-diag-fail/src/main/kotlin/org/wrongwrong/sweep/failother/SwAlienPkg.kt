package org.wrongwrong.sweep.failother

import org.wrongwrong.sweep.fail.SwRogueSi
import kotlin.reflect.KClass

// TC-MAN-057(a): 同一モジュール・別パッケージからの生成 Enumish 手動実装。
// sealed（V1 成立）の言語制約（同一パッケージ）に違反し、言語側エラーになる
object SwAlienPkg : SwRogueSi.Enumish {
    override val label: String get() = "alien"

    override val enumizedClass: KClass<out SwRogueSi> get() = SwRogueSi.R1::class
}
