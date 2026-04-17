package app.peter.s526.presentation.view.main.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.peter.s526.R
import app.peter.s526.databinding.ItemBookBinding
import app.peter.s526.domain.model.Book
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions

class SearchAdapter(
    private val glideManager: RequestManager,
    private val onClick: (Book) -> Unit,
) : RecyclerView.Adapter<SearchAdapter.BookViewHolder>() {

    private val bookList: MutableList<Book> = mutableListOf()

    fun setData(list: List<Book>) {
        bookList.clear()
        bookList.addAll(list)
        notifyDataSetChanged()
    }

    fun appendData(list: List<Book>) {
        val size = bookList.size
        bookList.addAll(list)
        notifyItemRangeInserted(size, list.size)
    }

    fun clearData() {
        bookList.clear()
        notifyDataSetChanged()
    }

    fun size(): Int = bookList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(
            ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.bind(position, book)
        holder.itemView.setOnClickListener { onClick(book) }
    }

    override fun getItemCount(): Int = bookList.size

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
