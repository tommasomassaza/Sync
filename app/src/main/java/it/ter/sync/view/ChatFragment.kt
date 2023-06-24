package it.ter.sync.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.databinding.FragmentChatBinding
import it.ter.sync.view.adapter.ChatAdapter
import it.ter.sync.viewmodel.ChatViewModel

class ChatFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val chatViewModel: ChatViewModel by activityViewModels()
    private var _binding: FragmentChatBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter

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

        chatAdapter = ChatAdapter(emptyList())
        recyclerView.adapter = chatAdapter


        initObservers()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera i messaggi esistenti dalla Firebase Realtime Database
        chatViewModel.retrieveChats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        chatViewModel.chatList.observe(viewLifecycleOwner) {
            chatAdapter.setChatList(it)
        }
    }
}