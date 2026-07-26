package org.wrongwrong.fixtures.vis.pub

// internal な中間 sealed（docs/test/ケース02-可視性.md VIS-21。
// 中間には何も生成されず、その可視性は生成・掲載・末端 kind 解決に影響しない）
internal sealed interface HiddenMid : VisRoot
