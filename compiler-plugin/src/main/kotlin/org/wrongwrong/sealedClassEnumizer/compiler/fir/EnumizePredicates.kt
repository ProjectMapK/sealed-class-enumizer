package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// 述語でマッチできるのは @Enumize が付いた基底だけである。末端・companion は無アノテーションのため、
// supertype からの判定（EnumizeRawSupertypeTracker / EnumizeHierarchyResolver）を併用する
// （docs/コンパイラプラグイン設計01.md §2）
object EnumizePredicates {
    val ENUMIZE: DeclarationPredicate = DeclarationPredicate.create {
        annotated(EnumizeNames.ENUMIZE_ANNOTATION_FQ_NAME)
    }
}
