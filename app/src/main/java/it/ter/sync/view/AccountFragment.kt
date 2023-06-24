package it.ter.sync.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import it.ter.sync.R
import it.ter.sync.databinding.FragmentAccountBinding
import it.ter.sync.databinding.FragmentLoginBinding
import it.ter.sync.viewmodel.UserViewModel

class AccountFragment : Fragment()  {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentAccountBinding? = null

    private var saveButtonClicked: Boolean = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initObservers()

        val buttonSave = binding.saveBtn
        binding.email.isEnabled = false

        buttonSave.setOnClickListener {
            val name = binding.name.text.toString().ifEmpty { binding.name.hint.toString() }
            val age = binding.age.text.toString().ifEmpty { binding.age.hint.toString() }
            val location = binding.location.text.toString().ifEmpty { binding.location.hint.toString() }

            saveButtonClicked = true
            userViewModel.updateUserInfo(name,age,location)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUserInfo()
    }

    private fun initObservers() {
        userViewModel.currentUser.observe(viewLifecycleOwner) {
            // clear serve a rimuovere il testo se l'utente dovesse cambiare fragment
            binding.name.text.clear()
            binding.email.text.clear()
            binding.age.text.clear()
            binding.location.text.clear()

            binding.name.hint = it?.name
            binding.email.hint = it?.email
            binding.age.hint = it?.age
            binding.location.hint = it?.location
        }

        userViewModel.userUpdated.observe(viewLifecycleOwner) { result ->
            if(saveButtonClicked){
                if(result) {
                    Toast.makeText(activity, "Save completed", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Save completed")
                } else {
                    Toast.makeText(activity, "Save failed", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Save failed")
                }
            }
        }
    }
}