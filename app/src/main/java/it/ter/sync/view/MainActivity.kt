package it.ter.sync.view

import android.os.Bundle
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
import android.content.Intent

import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import it.ter.sync.viewmodel.NotificationViewModel

class MainActivity : AppCompatActivity() {
    private val TAG: String = javaClass.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var imageView: ImageView
    private lateinit var cardView: CardView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    private lateinit var userViewModel: UserViewModel
    private lateinit var notificationViewModel: NotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)

        // Prendo le notifiche non ancora visualizzate
        notificationViewModel.retrieveNotifications(displayed = false)

        initObservers()

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
                R.id.homeFragment, R.id.accountFragment, R.id.chatFragment, R.id.loginFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.loginFragment) {
                binding.appBarMain.badgeLayout.visibility = View.INVISIBLE
            } else  {
                binding.appBarMain.badgeLayout.visibility = View.VISIBLE
            }
        }

        binding.appBarMain.notifyMenu.setOnClickListener {
            navController.navigate(R.id.notificationFragment)
        }

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            // Aggiungi il codice per visualizzare l'immagine nell'ImageView
            imageView.setImageURI(imageUri)
        }


        imageView.setOnClickListener {
            pickImageFromGallery()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            val imageUri = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imageView.setImageBitmap(bitmap)

            // Ora che hai il bitmap, puoi chiamare il metodo UpdateUser()
            userViewModel.updateUserImage(bitmap);
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Se mi trovo nel loginFragment non posso accedere al menù
        if (navController.currentDestination?.id == R.id.loginFragment) {
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

    fun updateNotificationOnChangeUser(){
        notificationViewModel.retrieveNotifications(displayed = false)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 101)
    }

    private fun initObservers() {
        Log.i(TAG, "Registering Observers: ViewModel? $notificationViewModel")
        notificationViewModel.notificationListNotDisplayed.observe(this) {
            if(it.isNotEmpty()) {
                binding.appBarMain.badgeNotification.visibility = View.VISIBLE
                binding.appBarMain.badgeNotification.text = it.size.toString()
            } else {
                binding.appBarMain.badgeNotification.visibility = View.INVISIBLE
            }
        }
    }
}

