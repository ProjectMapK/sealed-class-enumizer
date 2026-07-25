package org.wrongwrong.fixtures.explicitenum

import org.wrongwrong.sealedClassEnumizer.Enumize

// 明示 companion 付き enum 末端（TC-LEAF-100）: 自動生成せず既存 companion を kind として流用し、
// 3 つの entries 名前空間（Verb.entries / Protocol.Enumish.entries / kind = Verb.Companion）が併存する
@Enumize
sealed interface Protocol {
    enum class Verb : Protocol {
        GET,
        POST;

        companion object
    }

    data class Custom(val raw: String) : Protocol
}
