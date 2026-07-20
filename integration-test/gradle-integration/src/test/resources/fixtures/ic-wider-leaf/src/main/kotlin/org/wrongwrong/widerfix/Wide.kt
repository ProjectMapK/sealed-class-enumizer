package org.wrongwrong.widerfix

// 基底（internal）より広い public 末端。companion の可視性を末端未満へ落とす編集で
// ENUMIZE_KIND_TYPE_NOT_DENOTABLE（規則 3）を誘発する（docs/テストケース管理.md TC-IC-022）
class Wide : WSI {
    companion object
}
