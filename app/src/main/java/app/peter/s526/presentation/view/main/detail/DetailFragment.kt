package app.peter.s526.presentation.view.main.detail

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.databinding.FragmentDetailBinding
import app.peter.s526.domain.model.BookDetail
import app.peter.s526.presentation.util.viewBinding
import app.peter.s526.presentation.view.main.MainViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail) {

    private val viewModel: MainViewModel by activityViewModels()
    private val binding by viewBinding(FragmentDetailBinding::bind)
    private val glide: RequestManager by lazy { Glide.with(this) }
    private val args: DetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() isbn[${args.isbn}]")
        loadDetail(args.isbn)
    }

    private fun loadDetail(isbn: String) {
        viewModel.getDetailBook(isbn) { detail ->
            bindDetail(detail)
        }
    }

    private fun bindDetail(detail: BookDetail) = with(binding) {
        bookTitle.text = detail.title
        bookSubtitle.text = detail.subtitle
        bookAuthorPublisher.text = buildAuthorPublisher(detail)
        bookLanguageIsbn.text = buildLanguageIsbn(detail)
        bookInfo.text = buildInfo(detail)
        bookDescription.text = detail.desc
        bookUrl.text = detail.url

        detailImage.scaleType = ImageView.ScaleType.FIT_CENTER
        glide.load(detail.image)
            .apply(RequestOptions().error(R.drawable.book))
            .into(detailImage)

        fab.setOnClickListener { view -> toggleBookmark(detail, view) }
    }

    private fun toggleBookmark(detail: BookDetail, anchor: View) {
        if (viewModel.isBookmarked(detail)) {
            viewModel.deleteBookmark(detail)
            Snackbar.make(anchor, getString(R.string.delete_bookmark), Snackbar.LENGTH_SHORT).show()
        } else {
            viewModel.addBookmark(detail)
            Snackbar.make(anchor, getString(R.string.add_bookmark), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun buildAuthorPublisher(detail: BookDetail): String = buildString {
        if (detail.authors.isNotEmpty()) append(detail.authors)
        if (detail.publisher.isNotEmpty()) {
            if (isNotEmpty()) append(" / ")
            append(detail.publisher)
        }
    }

    private fun buildLanguageIsbn(detail: BookDetail): String = listOfNotNull(
        detail.language.takeIf { it.isNotEmpty() },
        detail.isbn13.takeIf { it.isNotEmpty() },
        detail.isbn10.takeIf { it.isNotEmpty() },
    ).joinToString(" / ")

    private fun buildInfo(detail: BookDetail): String = listOfNotNull(
        detail.pages.takeIf { it.isNotEmpty() && it != "0" }?.let { "$it pages" },
        detail.year.takeIf { it.isNotEmpty() },
    ).joinToString(" / ")

    companion object {
        private const val TAG = "DetailFragment"
    }
}
