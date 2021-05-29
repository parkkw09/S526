package app.peterkwp.s526.ui.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.peterkwp.s526.databinding.FragmentBookmarkBinding
import app.peterkwp.s526.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment: Fragment() {

    private lateinit var viewModel: MainViewModel

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBookmarkBinding.inflate(inflater, container, false).apply {

        }
        return binding.root
    }
}