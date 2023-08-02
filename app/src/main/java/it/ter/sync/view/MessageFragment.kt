package it.ter.sync.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import it.ter.sync.R
import it.ter.sync.database.message.MessageData
import it.ter.sync.databinding.FragmentMessageBinding
import it.ter.sync.view.adapter.MessageAdapter
import it.ter.sync.viewmodel.MessageViewModel
import java.util.*
import kotlin.collections.ArrayList

class MessageFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val messageViewModel: MessageViewModel by activityViewModels()
    private var _binding: FragmentMessageBinding? = null

    private lateinit var recyclerView: RecyclerView

    private lateinit var messengerId: String
    private lateinit var messengerName: String
    private lateinit var currentUserName: String

    private var groupIDs: ArrayList<String> = ArrayList()

    private lateinit var messageAdapter: MessageAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initObservers()

        messengerId = arguments?.getString("messengerId") ?: ""
        messengerName = arguments?.getString("messengerName") ?: ""
        currentUserName = arguments?.getString("currentUserName") ?: ""
        val userImageUrl = arguments?.getString("userImageUrl") ?: ""
        val messengerImageUrl = arguments?.getString("messengerImageUrl") ?: ""

        binding.textViewMessenger.text = messengerName

        Glide.with(root)
            .load(messengerImageUrl)
            .error(R.mipmap.ic_launcher)
            .into(binding.profileImage)

        recyclerView = binding.recyclerMessage
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // Imposta stackFromEnd su true per far partire l'Adapter dal fondo
        }
        recyclerView.layoutManager = layoutManager

        messageAdapter = MessageAdapter(emptyList(), messengerId, messengerName)
        recyclerView.adapter = messageAdapter

        groupIDs.add(messengerId)


        // Gestisci l'invio di nuovi messaggi quando viene premuto il pulsante di invio
        val sendButton = binding.sendButton
        sendButton.setOnClickListener {
            val messageInput = binding.messageInput.text.toString()
            if (messageInput.isNotEmpty()) {
                messageViewModel.sendMessage(messageInput,messengerId,userImageUrl,messengerImageUrl,groupIDs)
                binding.messageInput.text.clear()
                binding.messageInput.clearFocus()

                (activity as MainActivity).hideKeyboard()
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Recupera i messaggi esistenti
        messageViewModel.retrieveMessages(messengerId, messengerName, currentUserName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        messageViewModel.messageList.observe(viewLifecycleOwner) {
            messageAdapter.setMessageList(it)
            recyclerView.scrollToPosition(it.size - 1)
        }
    }
}