package io.github.projectmapk.mpp.fixtures.kindaccessor

// interface 末端の private companion（kind）→ 末端 interface 内にネストした IR-only アクセサ経由で load
// （interface メンバーは public / private のみ。nested object から private companion を intra-scope 参照する）
interface KaIface : KaRoot {
    private companion object
}
