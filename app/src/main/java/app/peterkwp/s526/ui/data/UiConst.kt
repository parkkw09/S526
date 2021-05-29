package app.peterkwp.s526.ui.data

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    TabType.S526_NEW,
    TabType.S526_BOOKMARK,
    TabType.S526_HISTORY
)
annotation class TabType {
    companion object {
        const val S526_NEW = 0
        const val S526_BOOKMARK = 1
        const val S526_HISTORY = 2
    }
}