package it.ter.sync.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.ter.sync.database.notify.NotificationData
import it.ter.sync.databinding.FragmentChatBinding
import it.ter.sync.view.adapter.NotificationAdapter
import it.ter.sync.viewmodel.NotificationViewModel

class NotificationFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private var _binding: FragmentChatBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter

    lateinit var notificationListInFragment: ArrayList<NotificationData>
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

        notificationAdapter = NotificationAdapter(emptyList())
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
                val position = viewHolder.adapterPosition

                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                val deletedNotification: NotificationData =
                    notificationListInFragment[position]

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                notificationListInFragment.removeAt(position)

                // below line is to notify our item is removed from adapter.
                notificationAdapter.notifyItemRemoved(position)

                //notificationViewModel.remove(deletedNotification.uid)

                // below line is to display our snackbar with action.
                Snackbar.make(recyclerView, "Deleted", Snackbar.LENGTH_LONG)
                    .setAction(
                        "Undo"
                    ) {
                        // adding on click listener to our action of snack bar.
                        // below line is to add our item to array list with a position.
                        notificationListInFragment.add(
                            position,
                            deletedNotification
                        )
                        // below line is to notify item is
                        // added to our adapter class.
                        notificationAdapter.notifyItemInserted(position)
                    }.show()
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(recyclerView)

        initObservers()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationViewModel.notificationsDisplayed()
        notificationViewModel.retrieveNotifications(displayed = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        notificationViewModel.notificationList.observe(viewLifecycleOwner) {
            notificationListInFragment = it as ArrayList<NotificationData>
            notificationAdapter.setNotificationList(it)
        }
    }
}