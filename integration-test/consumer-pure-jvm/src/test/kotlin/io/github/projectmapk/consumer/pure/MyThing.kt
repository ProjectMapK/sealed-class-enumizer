package io.github.projectmapk.consumer.pure

import io.github.projectmapk.fixtures.si.SI
import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.EnumishCompanion
import io.github.projectmapk.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

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
