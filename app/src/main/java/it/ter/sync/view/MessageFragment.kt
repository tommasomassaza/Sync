package it.ter.sync.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.databinding.FragmentMessageBinding
import it.ter.sync.view.adapter.MessageAdapter
import it.ter.sync.viewmodel.MessageViewModel

class MessageFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val messageViewModel: MessageViewModel by activityViewModels()
    private var _binding: FragmentMessageBinding? = null

    private lateinit var recyclerView: RecyclerView

    private var messengerId: String? = null
    private lateinit var messengerName: String
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

        messengerId = arguments?.getString("userId") ?: ""
        messengerName = arguments?.getString("userName") ?: ""

        binding.textViewMessenger.text = messengerName

        recyclerView = binding.recyclerMessage
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // Imposta stackFromEnd su true per far partire l'Adapter dal fondo
        }
        recyclerView.layoutManager = layoutManager

        messageAdapter = MessageAdapter(emptyList(), messengerId!!, messengerName)
        recyclerView.adapter = messageAdapter

        // Gestisci l'invio di nuovi messaggi quando viene premuto il pulsante di invio
        val sendButton = binding.sendButton
        sendButton.setOnClickListener {
            val messageInput = binding.messageInput.text.toString()
            if (messageInput.isNotEmpty()) {
                messageViewModel.sendMessage(messageInput,messengerId)
                binding.messageInput.text.clear()
                binding.messageInput.clearFocus()

                // Tiro giù la keyboard
                val inputMethodManager =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(root.windowToken, 0)
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera i messaggi esistenti
        messageViewModel.retrieveMessages(messengerId, messengerName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        messageViewModel.messageList.observe(viewLifecycleOwner) {
            messageAdapter.setMessageList(it)
        }
    }
}