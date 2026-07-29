package io.github.projectmapk.fixtures.order.flat

// 接頭辞対の短い側（docs/test/ケース03-順序.md ORD-01/ORD-06）。
// class 末端（自動生成 companion が kind）: kind ClassId は Foo.Companion となるが、
// '.'(46) < 'B'(66) により inheritors 順でも FooBar より先行が保たれる
class Foo : FlatRoot
