package io.github.projectmapk.probe.alias

// docs/test/ケース04-診断.md DIA-67: FQN 表記の付与は生成へ到達する（Main が entries を観測）
@io.github.projectmapk.sealedClassEnumizer.Enumize
sealed interface AaFq {
    data object F1 : AaFq
}
