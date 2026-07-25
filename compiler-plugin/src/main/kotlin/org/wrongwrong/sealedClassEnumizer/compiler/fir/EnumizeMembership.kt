package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.name.ClassId

// あるクラスの「@Enumize 階層への正常な所属（ちょうど 1 つの基底に属する）」を表す値オブジェクト。
// 非所属・複数階層への所属（MULTIPLE_FAMILIES エラー構成）は EnumizeHierarchyResolver.membershipOf が
// null を返して表現するため、このオブジェクトが存在する時点で base は常に一意である。
// 異常状態の内訳が必要な診断は resolver.basesOf を使う
class EnumizeMembership(val base: FirRegularClassSymbol, private val subjectIsSealed: Boolean) {
    // 末端 = 階層に属する非 sealed（docs/コンパイラプラグイン設計00.md §1）
    val isLeaf: Boolean
        get() = !subjectIsSealed

    val isIntermediate: Boolean
        get() = subjectIsSealed

    fun isMemberOf(baseClassId: ClassId): Boolean = base.classId == baseClassId
}
