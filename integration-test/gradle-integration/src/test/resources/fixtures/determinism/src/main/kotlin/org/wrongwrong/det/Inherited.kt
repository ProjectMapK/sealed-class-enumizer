package org.wrongwrong.det

// 2 原則-b: 親クラスの具象 toString を継承する kind には生成しない（"parent" 表示のまま）
object Inherited : WithToString(), S
