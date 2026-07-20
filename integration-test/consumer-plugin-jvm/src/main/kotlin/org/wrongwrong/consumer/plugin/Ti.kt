package org.wrongwrong.consumer.plugin

import org.wrongwrong.sealedClassEnumizer.Enumize

// 利用側モジュールで定義する自前の @Enumize 階層（docs/テストケース管理.md TC-XM-040）。
// プラグインが両側（producer-jvm と本モジュール）に載った状態で、他モジュールの生成 API と
// 併用できることの正値基線（拡張登録の共存・述語の非干渉）
@Enumize
sealed interface TI {
    data class Alpha(val v: Int) : TI

    data object Beta : TI
}
