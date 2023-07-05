package it.ter.sync.view

import android.app.DatePickerDialog
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.databinding.FragmentAccountBinding
import it.ter.sync.databinding.FragmentAccountFriendBinding
import it.ter.sync.view.adapter.PostAdapter
import it.ter.sync.viewmodel.UserViewModel
import java.util.*


class AccountFriendFragment : Fragment()  {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentAccountFriendBinding? = null
    private lateinit var FriendName: String
    private lateinit var FriendImage: String
    private lateinit var FriendAge: String
    private lateinit var FriendLocation: String
    private lateinit var FriendTag: String
    private lateinit var FriendTag2: String
    private lateinit var FriendTag3: String


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountFriendBinding.inflate(inflater, container, false)
        val root: View = binding.root
        FriendName = arguments?.getString("FriendName") ?: ""
        FriendAge = arguments?.getString("FriendAge") ?: ""
        FriendLocation = arguments?.getString("FriendLocation") ?: ""
        FriendTag = arguments?.getString("FriendTag") ?: ""
        FriendTag2 = arguments?.getString("FriendTag2") ?: ""
        FriendTag3 = arguments?.getString("FriendTag3") ?: ""
        FriendImage = arguments?.getString("FriendImage") ?: ""

        binding.nameFriend.setText(FriendName)
        binding.ageFriend.setText(FriendAge)
        binding.locationFriend.setText(FriendLocation)
        binding.tag1Friend.setText(FriendTag)
        binding.tag2Friend.setText(FriendTag2)
        binding.tag3Friend.setText(FriendTag3)

        initObservers()

        return root
    }




    private fun initObservers() {

    }
}