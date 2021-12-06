package app.peter.s526.ui.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.peter.s526.ui.book.NewBookFragment
import app.peter.s526.ui.bookmark.BookmarkFragment
import app.peter.s526.ui.data.TabType
import app.peter.s526.ui.history.HistoryFragment

class BookPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        TabType.S526_NEW to { NewBookFragment() },
        TabType.S526_BOOKMARK to { BookmarkFragment() },
        TabType.S526_HISTORY to { HistoryFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}