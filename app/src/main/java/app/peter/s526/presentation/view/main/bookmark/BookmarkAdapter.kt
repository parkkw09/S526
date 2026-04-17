package app.peter.s526.presentation.view.main.bookmark

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.databinding.ItemBookBinding
import app.peter.s526.domain.model.Book
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions

class BookmarkAdapter(
    private val glideManager: RequestManager,
    private val onClick: (Book) -> Unit,
) : RecyclerView.Adapter<BookmarkAdapter.BookViewHolder>(), ItemTouchHelperListener {

    private val bookList: MutableList<Book> = mutableListOf()

    fun setData(list: List<Book>) {
        bookList.clear()
        bookList.addAll(list)
        notifyDataSetChanged()
    }

    fun clearData() {
        bookList.clear()
        notifyDataSetChanged()
    }

    fun sortList() {
        bookList.sortBy { it.title }
        notifyDataSetChanged()
    }

    fun reverseSortList() {
        bookList.sortByDescending { it.title }
        notifyDataSetChanged()
    }

    fun getList(): List<Book> = bookList.toList()

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

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Log.d(TAG, "onItemMove() from[$fromPosition], to[$toPosition]")
        val tmp = bookList[fromPosition]
        bookList[fromPosition] = bookList[toPosition]
        bookList[toPosition] = tmp
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemSwipe(position: Int) {
        Log.d(TAG, "onItemSwipe() position[$position]")
        bookList.removeAt(position)
        notifyItemRemoved(position)
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

    companion object {
        private const val TAG = "BookmarkAdapter"
    }
}
