package io.github.projectmapk.sweep.hmpp

// docs/test/ケース04-診断.md DIA-11: 中間ソースセット（webMain）の基底に対し、派生ソースセット（jsMain）へ末端を逸脱。
// sealed は「同一ソースセット」を要求し「可視な派生ソースセット」では不足するため言語エラーになる
data object SwHmppJsLeaf : SwHmpp
