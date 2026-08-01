package io.github.projectmapk.fixtures.mid

// 2 つの中間 sealed を同時に実装する末端（companion 明示なし = 多重経路での候補判定も観測する）。
// MpLeft 経由の初出位置に 1 回だけ entries へ載る（docs/test/ケース01-生成と実行時API.md API-57）
class MpBoth : MpLeft, MpRight
