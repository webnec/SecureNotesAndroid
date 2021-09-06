package pl.webnec.securenotes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import pl.webnec.securenotes.R
import pl.webnec.securenotes.base.RecyclerViewClickListener
import pl.webnec.securenotes.databinding.FrameDeviceBinding
import pl.webnec.securenotes.models.DeviceData

class DevicesRecyclerViewAdapter(
    private var devices: List<DeviceData>,
    private val listener: RecyclerViewClickListener
) : RecyclerView.Adapter<DevicesRecyclerViewAdapter.ViewHolder>(){

    override fun getItemCount() = devices.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.frame_device,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.recyclerviewBinding.device = devices[position]
        holder.recyclerviewBinding.itemLayout.setOnClickListener {
            listener.onRecyclerViewItemClick(holder.recyclerviewBinding.itemLayout, devices[position])
        }
    }

    inner class ViewHolder(
        val recyclerviewBinding: FrameDeviceBinding
    ) : RecyclerView.ViewHolder(recyclerviewBinding.root)
}

