package it.ter.sync.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

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

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            // Aggiungi il codice per visualizzare l'immagine nell'ImageView
            binding.imageAccount.setImageURI(imageUri)
        }


        binding.imageAccount.setOnClickListener {
            pickImageFromGallery()
        }
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
            if (it != null) {
            val bitmap = base64ToBitmap(it.image)
            binding.imageAccount.setImageBitmap(bitmap)}

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

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 101)
    }

    fun base64ToBitmap(base64String: String): Bitmap? {
        try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 101) {
            val imageUri = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            binding.imageAccount.setImageBitmap(bitmap)

            // Ora che hai il bitmap, puoi chiamare il metodo UpdateUser()
            userViewModel.updateUserImage(bitmap)
        }
    }
}