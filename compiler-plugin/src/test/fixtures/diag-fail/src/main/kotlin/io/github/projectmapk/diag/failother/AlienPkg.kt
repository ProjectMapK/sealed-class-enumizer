package io.github.projectmapk.diag.failother

import kotlin.reflect.KClass
import io.github.projectmapk.diag.fail.MiSi

// docs/test/ケース04-診断.md DIA-56: 同一 module 別 pkg 形 → MIOH + 言語 sealed 制約エラーの併発
object AlienPkg : MiSi.Enumish {
    override val label: String get() = "alien"

    override val enumizedClass: KClass<out MiSi> get() = MiSi.Ok::class
}
