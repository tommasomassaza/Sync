package it.ter.sync.view

import android.annotation.SuppressLint
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
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import it.ter.sync.viewmodel.NotificationViewModel

class MainActivity : AppCompatActivity() {
    private val TAG: String = javaClass.simpleName
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var imageUser: ImageView
    private lateinit var nameUser: TextView

    private lateinit var userViewModel: UserViewModel
    private lateinit var notificationViewModel: NotificationViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)

        // Prendo le notifiche non ancora visualizzate
        notificationViewModel.retrieveNotificationsNotDisplayed()

        initObservers()

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.accountFragment, R.id.chatFragment, R.id.loginFragment
            ), drawerLayout
        )

        // Nascondi la tastiera quando si fa clic al di fuori della tastiera
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }
            false
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.signUpFragment) {
                binding.appBarMain.badgeLayout.visibility = View.INVISIBLE
            } else  {
                binding.appBarMain.badgeLayout.visibility = View.VISIBLE
            }
        }

        binding.appBarMain.notifyMenu.setOnClickListener {
            navController.navigate(R.id.notificationFragment)
        }

        imageUser = navView.findViewById(R.id.imageUser)
        nameUser = navView.findViewById(R.id.textNameUser)
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

    fun onChangeUser(){
        notificationViewModel.retrieveNotificationsNotDisplayed()

        userViewModel.getUserInfo()
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
        userViewModel.currentUser.observe(this) {
            nameUser.text = it?.name
            Glide.with(this)
                .load(it?.image)
                .into(imageUser)
        }
    }
}

