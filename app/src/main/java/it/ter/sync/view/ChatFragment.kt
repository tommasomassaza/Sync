package it.ter.sync.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.R
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.FragmentChatBinding
import it.ter.sync.view.adapter.ChatAdapter
import it.ter.sync.viewmodel.ChatViewModel
import it.ter.sync.viewmodel.UserViewModel

class ChatFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()
    private var _binding: FragmentChatBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var searchString: String = ""

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

        chatAdapter = ChatAdapter(emptyList(), UserData())
        recyclerView.adapter = chatAdapter



        // Aggiungi questo codice all'interno del metodo onCreateView del tuo Fragment
        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false // Rende attiva la SearchView
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Azioni da eseguire quando viene premuto il tasto "Invio" sulla tastiera
                chatViewModel.filterChatsAndGroups(searchString)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Azioni da eseguire quando il testo nella SearchView cambia
                // Qui puoi aggiornare la tua stringa con il nuovo testo
                searchString = ""
                searchString = newText ?: ""
                chatViewModel.filterChatsAndGroups(searchString)
                // O fai qualsiasi altra cosa con la stringa
                return true
            }
        })

        binding.btnSearch.setOnClickListener {
            chatViewModel.filterChatsAndGroups(searchString)
        }


        initObservers()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUserInfo()

        // Recupera i messaggi esistenti dalla Firebase Realtime Database
        chatViewModel.retrieveChatsAndGroups()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        chatViewModel.chatAndGroupList.observe(viewLifecycleOwner) {
            chatAdapter.setChatList(it)
        }
        userViewModel.currentUser.observe(viewLifecycleOwner) {
            chatAdapter.setCurrentUser(it)
        }
    }
}