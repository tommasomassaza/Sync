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
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.database.user.UserData
import it.ter.sync.databinding.FragmentGroupDetailsBinding
import it.ter.sync.view.adapter.ChatAdapter
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

        groupDetailsAdapter = GroupDetailsAdapter(emptyList())
        recyclerView.adapter = groupDetailsAdapter

        initObservers()


        messengerId = arguments?.getString("messengerId") ?: ""
        messengerName = arguments?.getString("messengerName") ?: ""
        messengerImageUrl = arguments?.getString("messengerImageUrl") ?: ""


        binding.imageGroup.setOnClickListener {
            pickImageFromGallery()
        }

        if(messengerImageUrl != null) {
            // Carica l'immagine nell'ImageView immediatamente
            Glide.with(requireContext())
                .load(messengerImageUrl)
                .error(R.mipmap.ic_launcher)
                .into(binding.imageGroup)
        }

        return root
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 101) {
            val imageUri = data?.data

            messageViewModel.updateGroupImageFromGroupDetails(messengerId, imageUri)

            if(imageUri != null) {
                // Carica l'immagine nell'ImageView immediatamente
                Glide.with(requireContext())
                    .load(imageUri)
                    .error(R.mipmap.ic_launcher)
                    .into(binding.imageGroup)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageViewModel.retrieveUsersInGroup(messengerId)

        // Setto il nome del gruppo
        binding.textGroupName.text = messengerName
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initObservers() {
        messageViewModel.groupUsersList.observe(viewLifecycleOwner) {
            groupDetailsAdapter.setGroupUsersList(it)
        }
    }
}