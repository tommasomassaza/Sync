package it.ter.sync.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
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
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import it.ter.sync.viewmodel.MessageViewModel
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
    private lateinit var messageViewModel: MessageViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

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

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.signUpFragment) {
                binding.appBarMain.badgeLayout.visibility = View.INVISIBLE
            } else {
                binding.appBarMain.badgeLayout.visibility = View.VISIBLE
            }

            binding.appBarMain.notifyMenu.isClickable = destination.id != R.id.notificationFragment
        }

        binding.appBarMain.notifyMenu.setOnClickListener {
            navController.navigate(R.id.notificationFragment)
        }

        val headerView = navView.getHeaderView(0)
        imageUser = headerView.findViewById(R.id.imageUser)
        nameUser = headerView.findViewById(R.id.textNameUser)


        // Prendo le notifiche non ancora visualizzate
        notificationViewModel.retrieveNotificationsNotDisplayed()

        userViewModel.getUserInfo()
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Se mi trovo nel loginFragment non posso accedere al menù
        if (navController.currentDestination?.id == R.id.loginFragment) {
            return false
        }
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Il menu laterale è aperto, chiudi il menu
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText || view is TextView) {
                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
            currentFocusView.clearFocus()
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
                .error(R.mipmap.ic_launcher)
                .into(imageUser)
        }
    }
}

