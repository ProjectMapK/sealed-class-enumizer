package io.github.projectmapk.exposureuse

import io.github.projectmapk.exposure.SI
import io.github.projectmapk.sealedClassEnumizer.label

// 生成 API（supertype に runtime-api の Enumish / Enumized を持つ）を参照する。
// runtime-api が producer から api 経由で推移取得できないと、これらの supertype が未解決になる
fun labels(): List<String> = SI.Enumish.entries.map { it.label }

fun labelOf(si: SI): String = si.label
