package app.peter.s526.application

import android.content.Context

object Utils {

    const val TAG_PREFIX = "S526_"

    fun convertAppName(context: Context, appName: String) = try {
        val manager = context.packageManager.getPackageInfo(context.packageName, 0)
        "$appName[${manager.versionName}]"
    } catch (e: Exception) {
        Log.e(TAG, "convertAppName() exception[${e.localizedMessage}]")
        appName
    }

    private const val TAG = "Utils"
}