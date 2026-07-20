package org.wrongwrong.consumer.pure

import org.wrongwrong.fixtures.SI
import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

// 基底 Enumish（runtime-api）の手動実装（docs/テストケース管理.md TC-XM-045 / TC-MAN-009 の消費側）。
// 非 sealed の public interface のため、プラグイン未適用の別モジュールでも無制約に実装できる
//（docs/概要.md §8 許容）。enumishCompanion / enumizedClass は型を満たす値を返せばよい
//（producer 側の FreeAgent と同形で SI 階層のものを借用）
object MyThing : Enumish {
    override val label: String get() = "MyThing"

    override val enumishCompanion: Enumish.Companion<Enumish> get() = SI.Enumish

    override val enumizedClass: KClass<out Enumized<*>> get() = SI.Foo::class
}
