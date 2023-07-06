import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.ter.sync.database.repository.UserRepository
import it.ter.sync.database.user.UserData
import it.ter.sync.viewmodel.UserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var firestore: FirebaseFirestore

    @Mock
    private lateinit var storage: FirebaseStorage

    private lateinit var viewModel: UserViewModel

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = UserViewModel(application).apply {
            this.userRepository = this@UserViewModelTest.userRepository
            this.firebaseAuth = this@UserViewModelTest.firebaseAuth
            this.fireStore = this@UserViewModelTest.firestore
            this.storage = this@UserViewModelTest.storage
        }
    }

    @Test
    fun testLogin() {
        // Simula il successo del login
        val email = "test@example.com"
        val password = "password"

        // Crea uno stub del metodo signInWithEmailAndPassword per restituire un Task di successo
        val successTask = mock(Task::class.java)
        `when`(successTask.isSuccessful).thenReturn(true)

        `when`(firebaseAuth.signInWithEmailAndPassword(email, password)).thenReturn(successTask)

        viewModel.login(email, password)

        // Verifica che il risultato del login sia true
        assertEquals(true, viewModel.loginResult.value)
    }

    @Test
    fun testIsUserLoggedIn() {
        // Simula un utente autenticato
        val mockUser = Mockito.mock(FirebaseUser::class.java)
        `when`(firebaseAuth.currentUser).thenReturn(mockUser)

        // Verifica che il metodo isUserLoggedIn ritorni true
        assertEquals(true, viewModel.isUserLoggedIn())

        // Simula un utente non autenticato
        `when`(firebaseAuth.currentUser).thenReturn(null)

        // Verifica che il metodo isUserLoggedIn ritorni false
        assertEquals(false, viewModel.isUserLoggedIn())
    }

    @Test
    fun testGetUserInfo() = runBlocking {
        val user = firebaseAuth.currentUser
        val userData = user?.uid?.let { UserData(uid = it, name = "John", email = "john@example.com") }

        // Stub del metodo getUserByUid per restituire l'utente simulato
        `when`(user?.uid?.let { userRepository.getUserByUid(it) }).thenReturn(userData)

        viewModel.getUserInfo()

        // Verifica che il valore corrente di currentUser sia uguale all'utente simulato
        assertEquals(userData, viewModel.currentUser.value)
    }

    // Aggiungi altri metodi di test per gli altri metodi della classe UserViewModel
}

