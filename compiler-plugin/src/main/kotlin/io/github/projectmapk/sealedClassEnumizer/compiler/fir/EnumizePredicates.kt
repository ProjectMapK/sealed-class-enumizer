package io.github.projectmapk.sealedClassEnumizer.compiler.fir

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeNames
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate

// 述語でマッチできるのは @Enumize が付いた基底だけである。末端・companion は無アノテーションのため、
// supertype からの判定（EnumizeRawSupertypeTracker / EnumizeHierarchyResolver）を併用する
// （docs/コンパイラプラグイン設計01.md §2）。
// ENUMISH_LABEL の登録は @EnumishLabel を compiler-required にして型・引数を早期に解決させるためで、
// 宣言の検出には使わない（label の読み取りはチェッカー・IR が付与先から直接行う）
object EnumizePredicates {
    val ENUMIZE: DeclarationPredicate = DeclarationPredicate.create {
        annotated(EnumizeNames.ENUMIZE_ANNOTATION_FQ_NAME)
    }

    val ENUMISH_LABEL: DeclarationPredicate = DeclarationPredicate.create {
        annotated(EnumizeNames.ENUMISH_LABEL_ANNOTATION_FQ_NAME)
    }
}
