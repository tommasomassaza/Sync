package it.ter.sync.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.FragmentGroupDetailsBinding
import it.ter.sync.view.adapter.GroupDetailsAdapter
import it.ter.sync.viewmodel.ChatViewModel
import it.ter.sync.viewmodel.MessageViewModel
import it.ter.sync.viewmodel.UserViewModel

class GroupDetailsFragment : Fragment() {

    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()
    private val messageViewModel: MessageViewModel by activityViewModels()
    private var _binding: FragmentGroupDetailsBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupDetailsAdapter: GroupDetailsAdapter


    private lateinit var messengerId: String
    private lateinit var messengerName: String
    private lateinit var messengerImageUrl: String
    

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerGroup
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        initObservers()


        messengerId = arguments?.getString("messengerId") ?: ""
        messengerName = arguments?.getString("messengerName") ?: ""
        messengerImageUrl = arguments?.getString("messengerImageUrl") ?: ""



        if(messengerImageUrl != null) {
            // Carica l'immagine nell'ImageView immediatamente
            Glide.with(requireContext())
                .load(messengerImageUrl)
                .error(R.mipmap.ic_launcher)
                .into(binding.imageGroup)
        }

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.retrieveUsersInGroup(messengerId)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initObservers() {
        chatViewModel.groupUsersList.observe(viewLifecycleOwner) {
            groupDetailsAdapter.setGroupUsersList(it)
        }
    }
}