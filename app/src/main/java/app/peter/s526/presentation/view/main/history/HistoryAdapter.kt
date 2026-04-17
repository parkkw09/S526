package app.peter.s526.presentation.view.main.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.peter.s526.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onClick: (String) -> Unit,
) : ListAdapter<String, HistoryAdapter.HistoryViewHolder>(QueryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val query = getItem(position)
        holder.bind(query)
        holder.itemView.setOnClickListener { onClick(query) }
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(query: String) {
            binding.searchHistory.text = query
        }
    }
}

private class QueryDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}
