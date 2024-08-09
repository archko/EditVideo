package com.thuypham.ptithcm.editvideo.ui.fragment.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thuypham.ptithcm.editvideo.MainApplication
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.databinding.ItemMenuBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.model.Menu

class MenuAdapter(
    private val onItemSelected: ((item: Menu) -> Unit)? = null,
) : ListAdapter<Menu, MenuAdapter.MenuItemViewHolder>(DiffCallback()) {

    companion object {
        val listMenu: ArrayList<Menu> = getMenu()

        private fun getMenu(): ArrayList<Menu> {
            return arrayListOf(
                Menu(
                    Menu.MENU_SPLIT_VIDEO,
                    MainApplication.instance.getString(R.string.menu_split_video)
                ),
                Menu(
                    Menu.MENU_EXTRACT_IMAGES,
                    MainApplication.instance.getString(R.string.menu_extract_images)
                ),
                /*Menu(
                    Menu.MENU_EXTRACT_AUDIO,
                    MainApplication.instance.getString(R.string.menu_extract_audio)
                ),
                Menu(
                    Menu.MENU_REVERSE_VIDEO,
                    MainApplication.instance.getString(R.string.menu_reverse_video)
                ),
                Menu(
                    Menu.MENU_CONVERT_TO_GIF,
                    MainApplication.instance.getString(R.string.menu_convert_to_gif)
                ),*/
                Menu(
                    Menu.MENU_CUT_VID,
                    MainApplication.instance.getString(R.string.menu_cut_video)
                ),

                Menu(
                    Menu.MENU_MERGE_VIDEO,
                    MainApplication.instance.getString(R.string.menu_merge_video)
                ),
                Menu(
                    Menu.MENU_RUN_CMD,
                    MainApplication.instance.getString(R.string.menu_run_cmd)
                ),
                Menu(
                    Menu.MENU_PLAY_HISTORY,
                    MainApplication.instance.getString(R.string.menu_play_history)
                ),
            )
        }
    }

    class MenuItemViewHolder(
        private val binding: ItemMenuBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Menu) {
            binding.apply {
                tvMenu.text = item.name
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MenuItemViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuItemViewHolder(binding).apply {
            binding.tvMenu.setOnSingleClickListener {
                onItemSelected?.invoke(currentList[absoluteAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Menu>() {
        override fun areItemsTheSame(oldItem: Menu, newItem: Menu) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Menu, newItem: Menu) =
            oldItem == newItem
    }
}