package io.github.projectmapk.fixtures.scope.target

import io.github.projectmapk.fixtures.scope.target.Holder.Base

// 競合形 (1): 明示 import された Holder.Base が同一 pkg のトップレベル囮 Base より優先される
// （docs/test/ケース01-生成と実行時API.md API-51）。この末端は Holder.Base の階層へ所属し entries に載る
class ViaImport : Base
