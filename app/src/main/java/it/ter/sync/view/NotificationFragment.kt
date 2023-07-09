package it.ter.sync.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.ter.sync.R
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.FragmentChatBinding
import it.ter.sync.view.adapter.NotificationAdapter
import it.ter.sync.viewmodel.NotificationViewModel
import it.ter.sync.viewmodel.UserViewModel


class NotificationFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private var _binding: FragmentChatBinding? = null


    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerChat
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        notificationAdapter = NotificationAdapter(emptyList(), UserData())
        recyclerView.adapter = notificationAdapter




        // on below line we are creating a method to create item touch helper
        // method for adding swipe to delete functionality.
        // in this we are specifying drag direction and position to right
        // on below line we are creating a method to create item touch helper
        // method for adding swipe to delete functionality.
        // in this we are specifying drag direction and position to right
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                // this method is called when we swipe our item to right direction.
                val position = viewHolder.adapterPosition

                // Ottenere l'oggetto deletedNotification dall'adapter.
                val deletedNotification: NotificationData = notificationAdapter.getNotificationAtPosition(position)

                notificationViewModel.remove(deletedNotification)
                notificationAdapter.notifyItemRemoved(position)
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(recyclerView)

        initObservers()

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUserInfo()

        notificationViewModel.notificationsDisplayed()
        notificationViewModel.retrieveNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        notificationViewModel.notificationList.observe(viewLifecycleOwner) {
            notificationAdapter.setNotificationList(it)
        }
        userViewModel.currentUser.observe(viewLifecycleOwner) {
            notificationAdapter.setCurrentUser(it)
        }
    }
}