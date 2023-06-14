package it.ter.sync.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import it.ter.sync.R
import it.ter.sync.databinding.FragmentLoginBinding
import it.ter.sync.viewmodel.UserViewModel

class LoginFragment : Fragment() {
    private val TAG: String = javaClass.simpleName
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentLoginBinding? = null

    private var loginButtonClicked: Boolean = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initObservers()

        // Inflate the layout for this fragment
        val signUpLink = binding.signUpLink
        val buttonLogin = binding.loginBtn

        buttonLogin.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            loginButtonClicked = true
            userViewModel.login(email,password)
        }

        signUpLink.setOnClickListener{
            println("ENTRATO IN LISTENER......")
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userViewModel.isUserLoggedIn()) {
            // Utente autenticato, se finisco nel loginFragment devo fare il logout
            performLogout()
        }
    }

    /**
     * it initializes the view model and the methods used to retrieve the live data for the interface
     */
    private fun initObservers() {
        Log.i(TAG, "Registering Observers: ViewModel? $userViewModel")
        userViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            if(loginButtonClicked) {
                loginButtonClicked = false
                if (result) {
                    Toast.makeText(activity, "Login successful", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Login Successful")

                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Toast.makeText(activity, "Wrong username or password", Toast.LENGTH_SHORT)
                        .show()
                    Log.i(TAG, "Login Failed")
                }
            }
        }
    }

    private fun performLogout() {
        userViewModel.logout()
        Toast.makeText(activity, "Logout successful", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Logout")
    }
}