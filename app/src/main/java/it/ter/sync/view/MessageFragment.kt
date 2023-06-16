package it.ter.sync.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.ViewUtils.hideKeyboard
import it.ter.sync.databinding.FragmentMessageBinding
import it.ter.sync.view.adapter.MessageAdapter
import it.ter.sync.viewmodel.MessageViewModel

class MessageFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val messageViewModel: MessageViewModel by activityViewModels()
    private var _binding: FragmentMessageBinding? = null


    private var userId: String? = null
    private lateinit var userName: String
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

        userId = arguments?.getString("userId") ?: ""
        userName = arguments?.getString("userName") ?: ""

        binding.textViewMessenger.text = userName

        val recyclerView = binding.recyclerMessage
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true // Imposta reverseLayout su true per far partire l'Adapter dal fondo
            stackFromEnd = true // Imposta stackFromEnd su true per far partire l'Adapter dal fondo
        }

        recyclerView.layoutManager = layoutManager
        messageAdapter = MessageAdapter(emptyList(), userId!!, userName)
        recyclerView.adapter = messageAdapter

        initObservers()

        // Gestisci l'invio di nuovi messaggi quando viene premuto il pulsante di invio
        val sendButton = binding.sendButton
        sendButton.setOnClickListener {
            val messageInput = binding.messageInput.text.toString()
            if (messageInput.isNotEmpty()) {
                messageViewModel.sendMessage(messageInput,userId)
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

        // Recupera i messaggi esistenti dalla Firebase Realtime Database
        messageViewModel.retrieveMessages(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initObservers() {
        messageViewModel.messageList.observe(viewLifecycleOwner) {
            messageAdapter.setMessageList(it)
            messageAdapter.notifyDataSetChanged()
        }
    }
}