package it.ter.sync.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import it.ter.sync.R
import it.ter.sync.databinding.FragmentSignupBinding
import it.ter.sync.viewmodel.UserViewModel


class SignUpFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentSignupBinding? = null

    private var signUpButtonClicked: Boolean = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initObservers()

        val buttonSignup: MaterialButton = root.findViewById(R.id.signUpBtn)
        val insertName : EditText = root.findViewById(R.id.nameSignUp)
        val insertEmail : EditText = root.findViewById(R.id.emailSignUp)
        val insertAge : EditText = root.findViewById(R.id.age)
        val insertLocation : EditText = root.findViewById(R.id.location)
        val insertPassword1 : EditText = root.findViewById(R.id.password1)
        val insertPassword2 : EditText = root.findViewById(R.id.password2)

        buttonSignup.setOnClickListener {
            signUpButtonClicked = true
            val name = insertName.text.toString()
            val email = insertEmail.text.toString()
            val location = insertLocation.text.toString()
            val age = insertAge.text.toString()
            val password1 = insertPassword1.text.toString()
            val password2 = insertPassword2.text.toString()
            if( name.isEmpty() || email.isEmpty() || age.isEmpty() ||
                location.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
                Toast.makeText(activity,"Inserire i campi mancanti",Toast.LENGTH_SHORT).show()
            } else if(password1 != password2) {
                Toast.makeText(activity,"Le password non corrispondono",Toast.LENGTH_SHORT).show()
            } else {
                userViewModel.register(name,age,location,email,password1)
            }
        }

        // Inflate the layout for this fragment
        return root
    }

    private fun initObservers() {
        Log.i(TAG, "Registering Observers: ViewModel? $userViewModel")
        userViewModel.registrationResult.observe(viewLifecycleOwner) { registrationResult ->
            if(signUpButtonClicked) {
                signUpButtonClicked = false
                if (registrationResult == "Success") {
                    Toast.makeText(activity, "Sign Up Success", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Sign Up Success")
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                } else {
                    Toast.makeText(activity, "$registrationResult", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "$registrationResult")
                }
            }
        }
    }

}