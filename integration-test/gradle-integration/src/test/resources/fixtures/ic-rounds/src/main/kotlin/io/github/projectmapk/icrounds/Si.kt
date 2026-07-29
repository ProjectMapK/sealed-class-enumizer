package io.github.projectmapk.icrounds

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 多ファイル sealed 階層の基底（連続編集ラウンドで毎回共連れ再コンパイルされる）
@Enumize
sealed interface SI
