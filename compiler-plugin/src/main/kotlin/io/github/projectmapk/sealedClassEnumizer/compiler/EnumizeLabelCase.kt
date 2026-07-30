package io.github.projectmapk.sealedClassEnumizer.compiler

// runtime-api の LabelCase のうち変換規則を持つ具体ケースの内部表現（docs/概要.md §4）。
// PROJECT_DEFAULT は「プロジェクト既定の読み込み」という指定であって変換規則を持たないため含めない
// （@Enumize の引数未指定・PROJECT_DEFAULT・未知のエントリ名は、消費側がプロジェクト既定へ解決する）。
// エントリ名は runtime-api の LabelCase と一致していなければならない（EnumizeLabelCaseTest がガードする）
enum class EnumizeLabelCase {
    AS_DECLARED {
        override fun convert(simpleName: String): String = simpleName
    },
    UPPER_SNAKE_CASE {
        // 大文字化は String.uppercase()（ロケール非依存）による
        override fun convert(simpleName: String): String =
            convertCamelCase(simpleName, '_').uppercase()
    },
    SNAKE_CASE {
        override fun convert(simpleName: String): String = convertCamelCase(simpleName, '_')
    },
    KEBAB_CASE {
        override fun convert(simpleName: String): String = convertCamelCase(simpleName, '-')
    };

    // 末端宣言の単純名から label への変換。label は永続化互換のためリリース後に変換結果を変えない
    abstract fun convert(simpleName: String): String

    companion object {
        // labelCase オプション欠落時の組み込み既定（docs/概要.md §4）
        val BUILT_IN_DEFAULT: EnumizeLabelCase = AS_DECLARED

        fun fromNameOrNull(name: String): EnumizeLabelCase? = entries.firstOrNull {
            it.name == name
        }
    }
}

// kotlinx.serialization の JsonNamingStrategy（SnakeCase / KebabCase）と同一の単語分割・小文字化。
// 大文字の連続は 1 語として保ち、直後に小文字が続く場合のみ末尾 1 文字を次語の頭とする
// （HTTPServer → http_server）。数字・区切り記号・非 ASCII 文字は語境界を作らずそのまま通し、
// 判定（Char.isUpperCase / Char.lowercaseChar）はロケールに依存しない
private fun convertCamelCase(simpleName: String, delimiter: Char): String =
    buildString(simpleName.length * 2) {
        var bufferedChar: Char? = null
        var previousUpperCharsCount = 0
        for (c in simpleName) {
            if (c.isUpperCase()) {
                if (previousUpperCharsCount == 0 && isNotEmpty() && last() != delimiter) {
                    append(delimiter)
                }
                bufferedChar?.let(::append)
                previousUpperCharsCount++
                bufferedChar = c.lowercaseChar()
            } else {
                val buffered = bufferedChar
                if (buffered != null) {
                    if (previousUpperCharsCount > 1 && c.isLetter()) {
                        append(delimiter)
                    }
                    append(buffered)
                    previousUpperCharsCount = 0
                    bufferedChar = null
                }
                append(c)
            }
        }
        bufferedChar?.let(::append)
    }
