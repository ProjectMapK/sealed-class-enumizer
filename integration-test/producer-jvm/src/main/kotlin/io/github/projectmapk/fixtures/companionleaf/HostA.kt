package io.github.projectmapk.fixtures.companionleaf

// 序数境界の対照末端（docs/test/ケース03-順序.md ORD-08）: p.Host.Companion と p.HostA は
// 共通接頭辞 "Host" の後 '.'(46) < 'A'(65) のため Host.Companion が HostA より先行する
data object HostA : Token
