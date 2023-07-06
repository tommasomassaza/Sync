package it.ter.sync.view

import android.app.DatePickerDialog
import android.content.Intent
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
import com.bumptech.glide.Glide
import it.ter.sync.R
import it.ter.sync.databinding.FragmentAccountBinding
import it.ter.sync.viewmodel.UserViewModel
import java.util.*


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
            val name = binding.name.text.toString().ifEmpty { binding.name.hint.toString()}
            val age = binding.age.text.toString().ifEmpty { binding.age.hint.toString() }
            val tag1 = binding.tag1.text.toString().ifEmpty { binding.tag1.hint.toString()}
            val tag2 = binding.tag2.text.toString().ifEmpty { binding.tag2.hint.toString()}
            val tag3 = binding.tag3.text.toString().ifEmpty { binding.tag3.hint.toString()}

            saveButtonClicked = true
            userViewModel.updateUserInfo(name,age,tag1.replace(" ", ""), tag2.replace(" ", ""), tag3.replace(" ", ""))
        }


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.getUserInfo()
        userViewModel.getUserImage()

        binding.imageAccount.setOnClickListener {
            pickImageFromGallery()
        }

        binding.age.setOnClickListener {
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
            binding.age.setText(selectedDate)
        }, year, month, day)

        // Imposta la data massima selezionabile la minima per avere 18 anni
        calendar.add(Calendar.YEAR, -18)
        val minDate = calendar.timeInMillis
        datePicker.datePicker.maxDate = minDate

        datePicker.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 101) {
            val imageUri = data?.data

            // Ora che hai il bitmap, puoi chiamare il metodo UpdateUser()
            userViewModel.updateUserImage(imageUri)
        }
    }

    private fun initObservers() {
        userViewModel.currentUser.observe(viewLifecycleOwner) {
            // clear serve a rimuovere il testo se l'utente dovesse cambiare fragment
            binding.name.text.clear()
            binding.email.text.clear()
            binding.age.text.clear()
            binding.tag1.text.clear()
            binding.tag2.text.clear()
            binding.tag3.text.clear()

            binding.name.hint = it?.name
            binding.email.hint = it?.email
            binding.age.hint = it?.age
            binding.tag1.hint = it?.tag
            binding.tag2.hint = it?.tag2
            binding.tag3.hint = it?.tag3
        }

        userViewModel.userImage.observe(viewLifecycleOwner) {
            Glide.with(this)
                .load(it)
                .error(R.mipmap.ic_launcher)
                .into(binding.imageAccount)
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