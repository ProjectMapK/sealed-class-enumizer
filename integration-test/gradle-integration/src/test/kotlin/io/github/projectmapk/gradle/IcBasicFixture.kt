package io.github.projectmapk.gradle

// ic-basic フィクスチャ（多ファイル分散のトップレベル基底 SI・独立階層 TI・非 sealed 外側クラス内の
// ネスト基底 NB・無関係ファイルの同居）の共有定数。
// IC 回帰テスト群（IcRegressionTest / IcCompanionTest）が参照する
// （docs/test/ケース06-ビルド動態.md BLD-01・docs/test/フィクスチャ構成.md §4）
object IcBasicFixture {
    const val NAME = "ic-basic"

    private const val SRC = "src/main/kotlin/io/github/projectmapk/icfix"
    const val SI_FILE = "$SRC/Si.kt"
    const val FOO_FILE = "$SRC/Foo.kt"
    const val BAR_FILE = "$SRC/Bar.kt"
    const val OUTER_FILE = "$SRC/Outer.kt"
    const val NB_FILE = "$SRC/Nb.kt"
    const val USE_FILE = "$SRC/Use.kt"
    const val MAIN_FILE = "$SRC/Main.kt"
    const val UNRELATED_FILE = "$SRC/Unrelated.kt"
    const val BAZ_FILE = "$SRC/Baz.kt"
    const val MID_FILE = "$SRC/Mid.kt"
    const val ROGUE_FILE = "$SRC/Rogue.kt"
    const val THIRD_FILE = "$SRC/Third.kt"
    const val TYPED_USE_FILE = "$SRC/TypedUse.kt"
    const val DUP_FILE = "$SRC/Dup.kt"

    // 生成 .class の相対パス接頭辞（クラスディレクトリ基準）
    const val CLASS_PREFIX = "io/github/projectmapk/icfix"

    // clean ビルドの実行時基準値（docs/概要.md §5 の FQN 序数順: Bar < Foo < Outer.Leaf）
    val BASELINE_OUT =
        listOf(
            "ENTRIES=Bar,Foo,Leaf",
            "TI_ENTRIES=T1",
            "NB_ENTRIES=N1",
            "DESCRIBE=foo,bar,leaf",
            "PROBE_FOO=Foo",
            "PROBE_LEAF=Leaf",
            "TRY_BAZ=IAE:No enumish entry with label 'Baz' in SI",
            "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI",
        )

    // SI 階層のファイル帰属を分類するための接頭辞（帰属は docs/コンパイラプラグイン設計00.md §4 の成果物対応表）
    fun isSiGenerated(key: String): Boolean = key.startsWith("$CLASS_PREFIX/SI$")

    fun isNbGenerated(key: String): Boolean = key.startsWith("$CLASS_PREFIX/NbHost\$NB$")

    fun isTiOutput(key: String): Boolean = key.startsWith("$CLASS_PREFIX/TI")

    fun isHierarchyOutput(key: String): Boolean =
        key.startsWith("$CLASS_PREFIX/SI") ||
            key.startsWith("$CLASS_PREFIX/Foo") ||
            key.startsWith("$CLASS_PREFIX/Bar") ||
            key.startsWith("$CLASS_PREFIX/Outer")
}
