package it.ter.sync.view.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.GroupItemBinding

class GroupDetailsAdapter (private var groupUsersList: List<UserData>) : RecyclerView.Adapter<GroupDetailsAdapter.ViewHolder>() {

    fun setGroupUsersList(groupUsers: List<UserData>) {
        groupUsersList = groupUsers
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }


    class ViewHolder(val binding: GroupItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            contactName.text = groupUsersList[position].name


            Glide.with(root)
                .load(groupUsersList[position].image)
                .error(R.mipmap.ic_launcher)
                .into(profileImage)
        }
    }

    override fun getItemCount(): Int {
        return groupUsersList.size
    }
}