package io.github.projectmapk.fixtures.mid

// 中間 sealed interface（entries には載らない。docs/test/ケース01-生成と実行時API.md API-30 の経由点）。
// 明示 companion は階層を実装せず、Enumish 非注入・kind 非成立のまま残る（API-30 の観測点）
sealed interface MidIface : RootVia {
    companion object
}
