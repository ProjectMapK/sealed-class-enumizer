package org.wrongwrong.fixtures.reentry

// 遅延初期化の順序センサ（TC-BOX-003）。kind の初期化が走ったことを記録する
object InitProbe {
    val events: MutableList<String> = mutableListOf()
}
