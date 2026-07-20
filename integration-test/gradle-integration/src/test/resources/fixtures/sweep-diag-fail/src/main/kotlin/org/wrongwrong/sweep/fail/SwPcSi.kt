package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-GAP-019: 非 final 末端（interface / fun interface）の private companion も
// entries 構築（基底帰属）から名前参照できず ENUMIZE_KIND_NOT_ACCESSIBLE
@Enumize
sealed interface SwPcSi {
    interface Iface : SwPcSi {
        private companion object
    }

    fun interface Fn : SwPcSi {
        fun run(): Int

        private companion object
    }
}
