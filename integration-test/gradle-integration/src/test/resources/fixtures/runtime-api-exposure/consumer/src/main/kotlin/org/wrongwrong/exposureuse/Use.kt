package org.wrongwrong.exposureuse

import org.wrongwrong.exposure.SI
import org.wrongwrong.sealedClassEnumizer.label

// 生成 API（supertype に runtime-api の Enumish / Enumized を持つ）を参照する。
// runtime-api が producer から api 経由で推移取得できないと、これらの supertype が未解決になる
fun labels(): List<String> = SI.Enumish.entries.map { it.label }

fun labelOf(si: SI): String = si.label
