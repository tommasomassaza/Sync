package it.ter.sync.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.FragmentChatBinding
import it.ter.sync.databinding.GroupCreationBinding
import it.ter.sync.view.adapter.ChatAdapter
import it.ter.sync.view.adapter.GroupAdapter
import it.ter.sync.viewmodel.ChatViewModel
import it.ter.sync.viewmodel.MessageViewModel
import it.ter.sync.viewmodel.UserViewModel

class GroupFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()
    private val messageViewModel: MessageViewModel by activityViewModels()
    private var _binding: GroupCreationBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private var groupImageUri: Uri? = null

    private var saveButtonClicked: Boolean = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GroupCreationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerGroup
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        groupAdapter = GroupAdapter(emptyList(), UserData(),messageViewModel)
        recyclerView.adapter = groupAdapter

        initObservers()

        binding.saveBtn.setOnClickListener {
            val name = binding.editTextGroupName.text.toString().ifEmpty { binding.editTextGroupName.hint.toString()}
            messageViewModel.createGroupWithUsers("",name)
            saveButtonClicked = true
        }

        binding.imageViewGroupIcon.setOnClickListener {
            pickImageFromGallery()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUserInfo()

        // Recupera i messaggi esistenti dalla Firebase Realtime Database
        chatViewModel.retrieveChats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 101) {
            val imageUri = data?.data

            groupImageUri = imageUri
            // Ora che hai il bitmap, puoi chiamare il metodo UpdateUser()
            //chatViewModel.updateGroupImage(imageUri)
        }
    }

    private fun initObservers() {
        chatViewModel.chatList.observe(viewLifecycleOwner) {
            groupAdapter.setChatList(it)
        }
        userViewModel.currentUser.observe(viewLifecycleOwner) {
            groupAdapter.setCurrentUser(it)
        }

        messageViewModel.groupCreated.observe(viewLifecycleOwner) { result ->
            if(saveButtonClicked){
                if(result) {
                    Toast.makeText(activity, "Group created", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Save completed")
                } else {
                    Toast.makeText(activity, "Save failed", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Save failed")
                }
            }
        }
    }
}