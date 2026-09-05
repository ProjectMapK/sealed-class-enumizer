package io.github.projectmapk.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.expressions.IrExpression

// createEntries が 1 つの kind を取得する式を組み立てるビルダ（呼び出し時の receiver は createEntries
// のビルダ）。EnumizeKindAccessorIrGenerator が kind ごとに作り、EnumizeEntriesHolderIrGenerator が
// createEntries のボディで評価する（docs/コンパイラプラグイン設計02.md §4.3）
typealias EnumizeKindProvider = IrBuilderWithScope.() -> IrExpression
