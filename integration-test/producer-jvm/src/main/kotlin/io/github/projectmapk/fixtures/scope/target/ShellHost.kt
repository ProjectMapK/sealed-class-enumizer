package io.github.projectmapk.fixtures.scope.target

import io.github.projectmapk.fixtures.scope.target.Holder.Base

// 競合形 (3): 明示 import よりホスト内側のネスト同名 Base が優先される
// （docs/test/ケース01-生成と実行時API.md API-51）。
// ViaNest は ShellHost.Base の実装となり、Holder.Base の階層へは所属しない
class ShellHost {
    interface Base

    class ViaNest : Base
}
