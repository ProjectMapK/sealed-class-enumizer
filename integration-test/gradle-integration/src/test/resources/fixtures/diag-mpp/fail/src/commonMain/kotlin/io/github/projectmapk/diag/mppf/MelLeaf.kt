package io.github.projectmapk.diag.mppf

// docs/test/ケース04-診断.md DIA-09: 末端が expect class。言語の sealed 制約エラー（different
// module）は actual 宣言側に出て、expect 側は無診断・ON_EXPECT / ON_ACTUAL も出ない
expect class MelLeaf() : MelSi
