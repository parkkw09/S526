package app.peterkwp.s526.util

object Log {

    fun i(tag: String, vararg objects: Any) {
        android.util.Log.i(Utils.TAG + tag, toString(*objects))
    }

    fun d(tag: String, vararg objects: Any) {
        android.util.Log.d(Utils.TAG + tag, toString(*objects))
    }

    fun w(tag: String, vararg objects: Any) {
        android.util.Log.w(Utils.TAG + tag, toString(*objects))
    }

    fun e(tag: String, vararg objects: Any) {
        android.util.Log.e(Utils.TAG + tag, toString(*objects))
    }

    fun v(tag: String, vararg objects: Any) {
        android.util.Log.v(Utils.TAG + tag, toString(*objects))
    }

    private fun toString(vararg objects: Any): String {
        val sb = StringBuilder()
        for (o in objects) {
            sb.append(o)
        }
        return sb.toString()
    }
}