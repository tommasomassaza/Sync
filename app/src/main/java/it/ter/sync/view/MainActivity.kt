package it.ter.sync.view

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import it.ter.sync.R
import it.ter.sync.databinding.ActivityMainBinding
import it.ter.sync.viewmodel.UserViewModel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap

import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val TAG: String = javaClass.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout




    private lateinit var imageView: ImageView
    private lateinit var cardView: CardView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    private val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1


    companion object {
        val IMAGE_REQUEST_CODE = 1_000;
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        setSupportActionBar(binding.appBarMain.toolbar)


        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.



        val headerView = navView.getHeaderView(0)
        imageView = headerView.findViewById(R.id.imageButton)
        cardView = headerView.findViewById(R.id.cardView)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.accountFragment, R.id.loginFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //imageButton = findViewById(R.id.imageButton)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            // Aggiungi il codice per visualizzare l'immagine nell'ImageView
            imageView.setImageURI(imageUri)
        }


        imageView.setOnClickListener {
            pickImageFromGallery()
        }

    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            val imageUri = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imageView.setImageBitmap(bitmap)
        }
    }









    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Se mi trovo nel loginFragment non posso accedere al menù
        val currentFragment = navController.currentDestination
        if (currentFragment?.id == R.id.loginFragment) {
            return false
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Il menu laterale è aperto, chiudi il menu
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}

