package it.ter.sync.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import it.ter.sync.R
import it.ter.sync.databinding.FragmentSignupBinding
import it.ter.sync.viewmodel.UserViewModel
import java.util.Calendar


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

        val buttonSignUp = binding.signUpBtn

        buttonSignUp.setOnClickListener {
            signUpButtonClicked = true

            val name = binding.nameSignUp.text.toString()
            val email = binding.emailSignUp.text.toString()
            val age = binding.ageSignUp.text.toString()
            val password1 = binding.password1.text.toString()
            val password2 = binding.password2.text.toString()

            if( name.isEmpty() || email.isEmpty() || age.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
                Toast.makeText(activity,"Inserire i campi mancanti",Toast.LENGTH_SHORT).show()
            } else if(password1 != password2) {
                Toast.makeText(activity,"Le password non corrispondono",Toast.LENGTH_SHORT).show()
            } else {
                userViewModel.register(name,age,email,password1)
            }
        }

        // Inflate the layout for this fragment
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ageSignUp.setOnClickListener{
            showDatePicker()
        }
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Mostra il DatePickerDialog per selezionare la data di nascita
        val datePicker = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
            // Il valore della data selezionata viene restituito qui
            val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
            binding.ageSignUp.setText(selectedDate)
        }, year, month, day)

        // Imposta la data massima selezionabile la minima per avere 18 anni
        calendar.add(Calendar.YEAR, -18)
        val minDate = calendar.timeInMillis
        datePicker.datePicker.maxDate = minDate

        datePicker.show()
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
                    Toast.makeText(activity, registrationResult, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, registrationResult)
                }
            }
        }
    }

}