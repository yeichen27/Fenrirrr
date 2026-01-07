package dev.ragnarok.fenrir.fragment.filemanagerselect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso3.Picasso
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.filePathToUrl
import dev.ragnarok.fenrir.model.FileItem
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.util.AppTextUtils.pluralNumerical
import dev.ragnarok.fenrir.util.Utils

class FileManagerSelectAdapter(private var data: List<FileItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var clickListener: ClickListener? = null

    fun setItems(data: List<FileItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> return FileHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_manager_folder, parent, false)
            )

            else -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_manager_file, parent, false)
            }
        }
        return FileHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_manager_file, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].isDir) 0 else 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindFileHolder(holder as FileHolder, position)
    }

    private fun onBindFileHolder(holder: FileHolder, position: Int) {
        val item = data[position]

        holder.fileName.text = item.file_name
        holder.fileDetails.text =
            if (!item.isDir) Utils.BytesToSize(item.size) else pluralNumerical(
                holder.fileDetails.context,
                item.size.toInt(),
                R.string.files_count_a,
                R.string.files_count_b,
                R.string.files_count_c
            )
        PicassoInstance.with()
            .load(item.file_path.filePathToUrl("thumb_file")).tag(Constants.PICASSO_TAG)
            .priority(Picasso.Priority.LOW)
            .into(holder.icon)
        holder.itemView.setOnClickListener {
            clickListener?.onClick(holder.bindingAdapterPosition, item)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onClick(position: Int, item: FileItem)
    }

    class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.item_file_name)
        val fileDetails: TextView = itemView.findViewById(R.id.item_file_details)
        val icon: ImageView = itemView.findViewById(R.id.item_file_icon)
    }
}
