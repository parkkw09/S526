package app.peterkwp.s526.ui.bookmark

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCallback(
    private val listener: ItemTouchHelperListener
): ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val drag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipe = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(drag, swipe)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onItemSwipe(viewHolder.adapterPosition)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }
}

interface ItemTouchHelperListener {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemSwipe(position: Int)
}
