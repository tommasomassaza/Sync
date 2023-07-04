package it.ter.sync.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.database.notify.NotificationType
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.ChatItemBinding
import it.ter.sync.databinding.NotificationItemBinding

class NotificationAdapter (private var notificationList: List<NotificationData>, private var currentUser: UserData) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    fun setNotificationList(notifications: List<NotificationData>) {
        notificationList = notifications
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }

    fun setCurrentUser(currentUser: UserData?) {
        if (currentUser != null) {
            this@NotificationAdapter.currentUser = currentUser
        }
    }

    class ViewHolder(val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.root.setOnClickListener {
            val navController = Navigation.findNavController(it)
            val bundle = Bundle()
            bundle.putString("messengerId", notificationList[position].notifierId)
            bundle.putString("messengerName", notificationList[position].notifierName)
            bundle.putString("currentUserName", currentUser.name)
            bundle.putString("userImageUrl", currentUser.image)
            bundle.putString("messengerImageUrl", notificationList[position].image)
            navController.navigate(R.id.action_notificationFragment_to_messageFragment, bundle)
        }

        holder.binding.apply {
            if(notificationList[position].type == NotificationType.MESSAGE){
                notificationMessage.text = notificationList[position].text
            }

            notificationTime.text = notificationList[position].timeStamp
            contactName.text = notificationList[position].notifierName

            if(notificationList[position].image != "") {
                Glide.with(root)
                    .load(notificationList[position].image)
                    .error(R.mipmap.ic_launcher)
                    .into(profileImage)
            }
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}