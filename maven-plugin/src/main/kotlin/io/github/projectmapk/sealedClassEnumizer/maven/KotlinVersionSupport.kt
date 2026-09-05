package io.github.projectmapk.sealedClassEnumizer.maven

// 対応外の Kotlin への適用を警告するための版判定。サポートはマイナー一致とする（同一 <major>.<minor>
// ならパッチ・プレリリースの差は対応内。版形式 <KotlinVersion>-<自版> の下で、成果物は対応マイナーの
// 全パッチへ同一物を配るため）。gradle-plugin と同じ判定を独立に持つ（両者に共通の上流モジュールが無い）
internal object KotlinVersionSupport {
    fun isSupported(appliedKotlinVersion: String, supportedKotlinVersion: String): Boolean =
        majorMinorOf(appliedKotlinVersion) == majorMinorOf(supportedKotlinVersion)

    private fun majorMinorOf(version: String): String = version.split('.').take(2).joinToString(".")
}
