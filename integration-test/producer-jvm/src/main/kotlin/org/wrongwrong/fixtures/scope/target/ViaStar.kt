package org.wrongwrong.fixtures.scope.target

import org.wrongwrong.fixtures.scope.other.*

// 競合形 (2): scope.other.* の star import より同一 pkg のトップレベル囮 Base が優先される
// （docs/test/ケース01-生成と実行時API.md API-51）。この末端は囮 interface の実装となり、
// scope.other.Base の階層へは所属しない。仮に scope.other.Base が解決されるなら sealed の
// 同一パッケージ制約で言語エラーになるため、コンパイル成功自体が同一 pkg 勝ちの観測である
class ViaStar : Base
