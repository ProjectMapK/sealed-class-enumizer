package io.github.projectmapk.fixtures.mid

// 中間 sealed class 経由の末端・companion 明示なし（docs/test/ケース01-生成と実行時API.md API-29）。
// raw 追跡再帰が `:MidClass()` → MidClass → RootVia へ到達し companion が自動生成される
class LeafViaMid(val v: Int) : MidClass()
