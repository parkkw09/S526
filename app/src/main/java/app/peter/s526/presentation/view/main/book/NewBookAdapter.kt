package app.peter.s526.presentation.view.main.book

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.peter.s526.R
import app.peter.s526.databinding.ItemBookBinding
import app.peter.s526.domain.model.NewBook
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions

class NewBookAdapter(
    private val glideManager: RequestManager,
    private val func : (NewBook) -> Unit
) : ListAdapter<NewBook, RecyclerView.ViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        Log.d(TAG, "onCreateViewHolder() viewType[$viewType]")
        return BookViewHolder(
            ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        Log.d(TAG, "onBindViewHolder() position[$position]")
        val book = getItem(position)
        (holder as BookViewHolder).apply {
            bind(position, book)
            itemView.setOnClickListener { func.invoke(book) }
        }
    }

    inner class BookViewHolder(
        private val binding: ItemBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(index: Int, item: NewBook) {
//            Log.d(TAG, "bind() item[$item]")
            val count = "#$index"
            with(binding) {
                this.bookTitle.text = item.title
                this.bookIsbn.text = item.isbn
                this.bookPrice.text = item.price
                this.bookUrl.text = item.url
                this.bookImage.let {
                    it.scaleType = ImageView.ScaleType.CENTER_CROP
                    glideManager
                        .load(item.image)
                        .apply(RequestOptions().error(R.drawable.book))
                        .into(it)
                }
                this.count.text = count
            }
        }
    }

    companion object {
        private const val TAG = "NewBookAdapter"
    }
}

private class BookDiffCallback : DiffUtil.ItemCallback<NewBook>() {

    override fun areItemsTheSame(oldItem: NewBook, newItem: NewBook): Boolean {
        return oldItem.isbn == newItem.isbn
    }

    override fun areContentsTheSame(oldItem: NewBook, newItem: NewBook): Boolean {
        return oldItem == newItem
    }
}