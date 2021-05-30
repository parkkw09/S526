package app.peterkwp.s526.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import app.peterkwp.s526.R
import app.peterkwp.s526.databinding.FragmentViewPagerBinding
import app.peterkwp.s526.ui.data.TabType
import app.peterkwp.s526.ui.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment: Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager
        val search = binding.search

        viewPager.adapter = BookPagerAdapter(this)

        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        search.setOnClickListener {
            val direction = ViewPagerFragmentDirections.actionViewPagerFragmentToSearchFragment("")
            viewModel.setCurrentSearchQuery("")
            it.findNavController().navigate(direction)
        }

        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            TabType.S526_NEW -> R.drawable.book
            TabType.S526_BOOKMARK -> R.drawable.bookmark
            TabType.S526_HISTORY -> R.drawable.history
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            TabType.S526_NEW -> getString(R.string.new_book)
            TabType.S526_BOOKMARK -> getString(R.string.bookmark)
            TabType.S526_HISTORY -> getString(R.string.history)
            else -> null
        }
    }
}