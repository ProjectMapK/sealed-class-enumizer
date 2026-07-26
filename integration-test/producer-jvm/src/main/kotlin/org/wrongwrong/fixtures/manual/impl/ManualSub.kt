package org.wrongwrong.fixtures.manual.impl

// open 手動実装の下流サブタイプ（docs/test/ケース01-生成と実行時API.md API-41）。
// 生成 Enumish を直接実装しないため inheritors には登載されず、既存の is ManualLeaf 枝が被覆する
class ManualSub(v: Int) : ManualLeaf(v)
