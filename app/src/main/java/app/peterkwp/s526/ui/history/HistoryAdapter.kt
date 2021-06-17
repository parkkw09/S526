package app.peterkwp.s526.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.peterkwp.s526.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val func : (String) -> Unit
) : ListAdapter<String, RecyclerView.ViewHolder>(QueryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        Log.d(TAG, "onCreateViewHolder() viewType[$viewType]")
        return HistoryViewHolder(
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        Log.d(TAG, "onBindViewHolder() position[$position]")
        val query = getItem(position)
        (holder as HistoryViewHolder).apply {
            bind(query)
            itemView.setOnClickListener { func.invoke(query) }
        }
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(query: String) {
//            Log.d(TAG, "bind() item[$item]")
            with(binding) {
                this.searchHistory.text = query
            }
        }
    }

    companion object {
        private const val TAG = "HistoryAdapter"
    }
}

private class QueryDiffCallback : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}