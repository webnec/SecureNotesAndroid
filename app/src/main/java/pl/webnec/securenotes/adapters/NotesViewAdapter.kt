package pl.webnec.securenotes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import pl.webnec.securenotes.R
import pl.webnec.securenotes.databinding.FrameMessageBinding
import pl.webnec.securenotes.base.RecyclerViewClickListener

class NotesViewAdapter(
        private var notes: List<String>,
        private val listener: RecyclerViewClickListener
) : RecyclerView.Adapter<NotesViewAdapter.ViewHolder>(){

    fun setNotes(notes: List<String>){
        this.notes = notes
        notifyDataSetChanged()
    }

    override fun getItemCount() = notes.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.frame_message,
                        parent,
                        false
                )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.recyclerviewBinding.message = notes[position]
        holder.recyclerviewBinding.itemLayout.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewBinding.itemLayout, position)
        }
    }

    inner class ViewHolder(
            val recyclerviewBinding: FrameMessageBinding
    ) : RecyclerView.ViewHolder(recyclerviewBinding.root)
}