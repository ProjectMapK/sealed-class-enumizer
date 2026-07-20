package org.wrongwrong.sweep.mel

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MPP-049 用の基底（非 expect。@Enumize の付与先は通常宣言のため ON_EXPECT / ON_ACTUAL は非発火）
@Enumize
sealed interface SwMel {
    data object Base : SwMel
}
