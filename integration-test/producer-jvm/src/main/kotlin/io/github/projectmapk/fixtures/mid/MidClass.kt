package io.github.projectmapk.fixtures.mid

// 中間 sealed class（entries には載らない。docs/test/ケース01-生成と実行時API.md API-30 の経由点）。
// companion は自身が RootVia を実装する末端であり成立する（API-52。中間 companion 末端の成立形）
sealed class MidClass : RootVia {
    companion object : RootVia
}
