package io.github.projectmapk.sweep.xn

import io.github.projectmapk.sealedClassEnumizer.Enumize
import kotlin.reflect.KClass

// docs/test/ケース05-境界横断.md XMP-13 用: public 基底 + internal な階層内手動実装。
// 手動実装は inheritors に載るが跨モジュールでは不可視のため、消費側の kind-when は else 必須になる。
// 1 ファイル 1 クラス規約は「階層内手動実装が同一階層に閉じる」構成要求を優先して適用外とする
@Enumize
sealed interface SwXnMan {
    data object Only : SwXnMan
}

internal class SwXnManImpl(val v: Int) : SwXnMan, SwXnMan.Enumish {
    override val label: String get() = "SwXnManImpl"

    override val enumizedClass: KClass<out SwXnMan> get() = SwXnManImpl::class
}
