package io.github.projectmapk.sweep.xn

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース05-境界横断.md XMP-13 用: public 基底 + public / internal 末端の混在
@Enumize
sealed interface SwXnMix

// 外側から名指しできる public 末端
data object SwXnPub : SwXnMix

// 外側から名指しできず else を強制する internal 末端
internal data object SwXnSec : SwXnMix
