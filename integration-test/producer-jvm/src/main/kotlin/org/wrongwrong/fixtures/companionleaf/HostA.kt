package org.wrongwrong.fixtures.companionleaf

// 順序境界の対照（TC-ORD-057）: p.Host.Companion と p.HostA は共通接頭辞 "Host" の後
// '.'(46) < 'A'(65) のため Host.Companion が HostA より先行する
data object HostA : Token
