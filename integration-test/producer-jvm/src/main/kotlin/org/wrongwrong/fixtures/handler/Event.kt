package org.wrongwrong.fixtures.handler

import org.wrongwrong.sealedClassEnumizer.Enumize

// fun interface 末端（V10-c: SAM 保持）のフィクスチャ
// （TC-LEAF-010 / TC-LEAF-038 = 明示 companion、TC-LEAF-081 = companion 自動生成、TC-BOX-068）
@Enumize
sealed interface Event {
    // 明示 companion 付き fun interface 末端。asEnumish は default 実装で生成され SAM の抽象は handle のみ
    fun interface Handler : Event {
        fun handle(x: Int): Int

        companion object
    }

    // companion 明示なしの fun interface 末端（自動生成 + SAM 保持の交差 = TC-LEAF-081）
    fun interface Listener : Event {
        fun onEvent(x: Int): Int
    }
}
