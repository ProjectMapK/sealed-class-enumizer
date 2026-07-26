package org.wrongwrong.consumer.pure

import kotlin.reflect.KClass
import org.wrongwrong.fixtures.si.SI
import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.EnumishCompanion
import org.wrongwrong.sealedClassEnumizer.Enumized

// 基底 Enumish（runtime-api）の自由実装（docs/test/ケース05-境界横断.md XMP-26 の消費側フィクスチャ）。
// 非 sealed の public interface のため、プラグイン未適用の別モジュールでも無制約に実装できる。
// enumishCompanion / enumizedClass は型を満たす値を返せばよい（producer 側の FreeAgent と同形で SI 階層を借用）
object MyThing : Enumish {
    override val label: String
        get() = "MyThing"

    override val enumishCompanion: EnumishCompanion<Enumish>
        get() = SI.Enumish

    override val enumizedClass: KClass<out Enumized<*>>
        get() = SI.Foo::class
}
