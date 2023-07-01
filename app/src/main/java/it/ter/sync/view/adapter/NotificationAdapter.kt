package it.ter.sync.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.database.chat.ChatData
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.database.notify.NotificationType
import it.ter.sync.databinding.ChatItemBinding
import it.ter.sync.databinding.NotificationItemBinding

class NotificationAdapter (private var notificationList: List<NotificationData>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    fun setNotificationList(notifications: List<NotificationData>) {
        notificationList = notifications
        notifyDataSetChanged() // Aggiorna la RecyclerView
    }

    class ViewHolder(val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            if(notificationList[position].type == NotificationType.MESSAGE){
                notificationMessage.text = notificationList[position].text
            }

            notificationTime.text = notificationList[position].timeStamp
            contactName.text = notificationList[position].userName
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}