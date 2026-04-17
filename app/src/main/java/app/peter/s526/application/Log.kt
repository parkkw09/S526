package app.peter.s526.application

import app.peter.s526.BuildConfig

/**
 * `BuildConfig.ENABLE_LOGGING` 플래그로 제어되는 로그 래퍼.
 * release 빌드에서는 `i/d/v/w` 호출이 모두 no-op 이므로 민감 정보가 노출되지 않는다.
 * 오류 로그(`e`)는 버그 분석을 위해 항상 출력한다.
 */
object Log {

    fun i(tag: String, vararg objects: Any) {
        if (!BuildConfig.ENABLE_LOGGING) return
        android.util.Log.i(Utils.TAG_PREFIX + tag, concat(*objects))
    }

    fun d(tag: String, vararg objects: Any) {
        if (!BuildConfig.ENABLE_LOGGING) return
        android.util.Log.d(Utils.TAG_PREFIX + tag, concat(*objects))
    }

    fun v(tag: String, vararg objects: Any) {
        if (!BuildConfig.ENABLE_LOGGING) return
        android.util.Log.v(Utils.TAG_PREFIX + tag, concat(*objects))
    }

    fun w(tag: String, vararg objects: Any) {
        if (!BuildConfig.ENABLE_LOGGING) return
        android.util.Log.w(Utils.TAG_PREFIX + tag, concat(*objects))
    }

    fun e(tag: String, vararg objects: Any) {
        android.util.Log.e(Utils.TAG_PREFIX + tag, concat(*objects))
    }

    private fun concat(vararg objects: Any): String = buildString {
        objects.forEach { append(it) }
    }
}
