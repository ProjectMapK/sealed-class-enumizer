// ルートは KGP をクラスローダへ一度だけロードして共有する（integration-test ルートと同じ方針）
plugins {
    kotlin("jvm") apply false
}
