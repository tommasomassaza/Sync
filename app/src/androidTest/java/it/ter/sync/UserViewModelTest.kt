package it.ter.sync

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import it.ter.sync.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserViewModelTest {

    private lateinit var context: Context
    private lateinit var userViewModel: UserViewModel

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        userViewModel = UserViewModel(context.applicationContext as Application)
    }


    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("it.ter.sync", appContext.packageName)
    }
    @Test
    fun testLogin() {
        userViewModel.register("test","11/8/1998", "example@gmail.com", "password")
        assertEquals("Success", userViewModel.registrationResult.value)
    }

    // Aggiungi altri metodi di test per gli altri metodi della classe UserViewModel
}

