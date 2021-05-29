package app.peterkwp.s526.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import app.peterkwp.s526.R
import app.peterkwp.s526.databinding.FragmentDetailBinding
import app.peterkwp.s526.domain.model.DetailBook
import app.peterkwp.s526.ui.viewmodel.MainViewModel
import app.peterkwp.s526.util.GlideApp
import app.peterkwp.s526.util.Log
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment: Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val glide: RequestManager by lazy { GlideApp.with(this) }
    private val args: DetailFragmentArgs by navArgs()

    private lateinit var binding: FragmentDetailBinding

    private fun launchUi(isbn: String) {
        Log.d(TAG, "launchUi()")
        viewModel.getDetailBook(isbn) { information ->
            binding.apply {
                val authorPublisher = "${information.authors} / ${information.publisher}"
                val languageIsbn = "${information.language} / ${information.isbn13} / ${information.isbn10}"
                val info = "${information.pages} / ${information.year} / ${information.rating} / ${information.price}"
                this.bookTitle.text = information.title
                this.bookSubtitle.text = information.subtitle
                this.bookAuthorPublisher.text = authorPublisher
                this.bookLanguageIsbn.text = languageIsbn
                this.bookInfo.text = info
                this.bookDescription.text = information.desc
                this.bookUrl.text = information.url
                this.bookPdfLink.text = information.pdf.freeBook
                this.detailImage.let {
                    it.scaleType = ImageView.ScaleType.FIT_CENTER
                    glide.load(information.image)
                        .apply(RequestOptions().error(R.drawable.book))
                        .into(it)
                }

                this.fab.setOnClickListener { view -> checkBookmark(isbn, information, view) }
            }
        }
    }

    private fun checkBookmark(isbn: String, information: DetailBook, view: View) {
        when (viewModel.checkBookmark(isbn)) {
            true -> {
                viewModel.deleteBookmark(isbn)
                Snackbar.make(view, getString(R.string.delete_bookmark), Snackbar.LENGTH_SHORT).show()
            }
            false -> {
                viewModel.addBookmark(isbn, information)
                Snackbar.make(view, getString(R.string.add_bookmark), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")
        launchUi(args.isbn)
    }

    companion object {
        private const val TAG = "DetailFragment"
    }
}