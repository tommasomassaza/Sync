package it.ter.sync.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.databinding.FragmentAccountFriendBinding
import it.ter.sync.viewmodel.UserViewModel


class AccountFriendFragment : Fragment()  {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentAccountFriendBinding? = null


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

        initObservers()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("userId") ?: ""
        val userName = arguments?.getString("userName") ?: ""
        val userImageUrl = arguments?.getString("userImageUrl") ?: ""
        val currentUserName = arguments?.getString("currentUserName") ?: ""
        val currentUserImageUrl = arguments?.getString("currentUserImageUrl") ?: ""

        binding.chatButton.setOnClickListener {
            val navController = Navigation.findNavController(it)
            val bundle = Bundle()
            bundle.putString("messengerId", userId)
            bundle.putString("messengerName", userName)
            bundle.putString("messengerImageUrl", userImageUrl)
            bundle.putString("currentUserName", currentUserName)
            bundle.putString("userImageUrl", currentUserImageUrl)
            navController.navigate(R.id.action_accountFriendFragment_to_messageFragment, bundle)
        }

        userViewModel.getUserInfo()
        userViewModel.getUserInfo(userId)
    }

    private fun initObservers() {
        userViewModel.accountFriend.observe(viewLifecycleOwner) {
            binding.nameFriend.text = it?.name
            binding.locationFriend.text = it?.location
            binding.ageFriend.text = it?.age
            binding.tag1Friend.text = it?.tag
            binding.tag2Friend.text = it?.tag2
            binding.tag3Friend.text = it?.tag3

            Glide.with(this)
                .load(it?.image)
                .error(R.mipmap.ic_launcher)
                .into(binding.imageAccountFriend)
        }
    }
}