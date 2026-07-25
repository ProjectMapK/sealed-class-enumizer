package org.wrongwrong.mpp.fixtures.order

import org.wrongwrong.sealedClassEnumizer.Enumize

// entries 順序（FQN 序数順）の MPP 版フィクスチャ（docs/概要.md §5 の実測形・
// docs/テストケース管理.md TC-MPP-006）。ネスト末端 S.Aaa / Box.Bbb とトップレベル末端
// Mmm / Zzz / aLower を混在させ、entries = [Bbb, Mmm, Aaa, Zzz, aLower]（FQN 序数順）が
// 全ターゲットで一致することを観測する。継承者はファイル分散で定義する
@Enumize sealed interface FqnOrder
