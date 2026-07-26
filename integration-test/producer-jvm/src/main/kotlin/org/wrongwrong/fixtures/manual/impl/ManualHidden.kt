package org.wrongwrong.fixtures.manual.impl

// 可視性の低い階層内手動実装（internal 形。docs/test/ケース01-生成と実行時API.md API-40）。
// object 末端の生成 Enumish 冗長宣言は注入スキップで許容され（ケース04 DIA-51 の末端 Enumish
// 冗長宣言と同形）、kind は自身のまま entries に掲載される。メンバーの手動宣言は
// ENUMIZE_MANUAL_MEMBER_CONFLICT になるため置かない。internal のため跨 module の kind-when では
// else が必要になる（ケース05 が正典）
internal object ManualHidden : WithManual, WithManual.Enumish
