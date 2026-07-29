package io.github.projectmapk.fixtures.vis.pub

// トップレベル末端 × private companion（docs/test/ケース02-可視性.md VIS-17 = ネスト壁:
// kind は末端クラス内の IR-only アクセサ経由で掲載され、asEnumish の返り値型は規則 2）
class PrivComp(val v: Int) : VisRoot {
    private companion object
}
