package com.github.cgang.syncfiles.presentation.login

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.cgang.syncfiles.data.repository.AuthRepository
import com.github.cgang.syncfiles.data.repository.AuthRepositoryImpl
import com.github.cgang.syncfiles.domain.usecase.LoginUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var viewModel: LoginViewModel
    private val mockAuthRepository: AuthRepository = mockk()
    private val mockLoginUseCase: LoginUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { mockLoginUseCase(any(), any()) } returns kotlinx.coroutines.flow.flowOf(
            com.github.cgang.syncfiles.domain.model.User(
                id = 1,
                username = "testuser",
                email = "test@example.com"
            )
        )

        viewModel = LoginViewModel(mockLoginUseCase, mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_success_updatesUiStateToSuccess() = testScope.runTest {
        coEvery { mockLoginUseCase("testuser", "password") } returns com.github.cgang.syncfiles.domain.model.User(
            id = 1,
            username = "testuser",
            email = "test@example.com"
        )

        viewModel.login("testuser", "password")

        val state = viewModel.uiState.value
        assert(state is com.github.cgang.syncfiles.presentation.login.LoginUiState.Success)
    }

    @Test
    fun login_withEmptyCredentials_showsError() = testScope.runTest {
        viewModel.login("", "")

        val state = viewModel.uiState.value
        assert(state is com.github.cgang.syncfiles.presentation.login.LoginUiState.Error)
    }
}
