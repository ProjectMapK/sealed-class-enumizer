package io.github.projectmapk.fixtures.vis.pub

// public enum class 末端 × internal companion（docs/test/ケース02-可視性.md VIS-14 = 規則 2 × 種別。
// enum 全体で 1 kind・name と label の分離は維持される）
enum class CmdFall : VisRoot {
    GO,
    STOP;

    internal companion object
}
