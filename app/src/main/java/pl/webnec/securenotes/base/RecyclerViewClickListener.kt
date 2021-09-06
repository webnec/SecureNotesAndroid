package pl.webnec.securenotes.base

import android.view.View

interface RecyclerViewClickListener {
    fun onRecyclerViewItemClick(view: View, T: Any)
}