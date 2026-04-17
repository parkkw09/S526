package app.peter.s526.presentation.view.main.book

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.peter.s526.R
import app.peter.s526.databinding.ItemBookBinding
import app.peter.s526.domain.model.Book
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions

class NewBookAdapter(
    private val glideManager: RequestManager,
    private val onClick: (Book) -> Unit,
) : ListAdapter<Book, NewBookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(
            ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(position, book)
        holder.itemView.setOnClickListener { onClick(book) }
    }

    inner class BookViewHolder(
        private val binding: ItemBookBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(index: Int, item: Book) = with(binding) {
            bookTitle.text = item.title
            bookSubtitle.text = item.subtitle
            count.text = "#${index + 1}"
            bookImage.scaleType = ImageView.ScaleType.CENTER_CROP
            glideManager
                .load(item.image)
                .apply(RequestOptions().error(R.drawable.book))
                .into(bookImage)
        }
    }
}

private class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean =
        oldItem.isbn == newItem.isbn

    override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean =
        oldItem == newItem
}
