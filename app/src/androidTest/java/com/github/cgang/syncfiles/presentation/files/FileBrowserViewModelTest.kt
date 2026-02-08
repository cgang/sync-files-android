package com.github.cgang.syncfiles.presentation.files

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.cgang.syncfiles.data.repository.FileRepository
import com.github.cgang.syncfiles.data.repository.FileRepositoryImpl
import com.github.cgang.syncfiles.domain.usecase.GetFilesUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FileBrowserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var viewModel: FileBrowserViewModel
    private val mockFileRepository: FileRepository = mockk()
    private val mockGetFilesUseCase: GetFilesUseCase = mockk()
    private val mockDownloadFileUseCase: com.github.cgang.syncfiles.domain.usecase.DownloadFileUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = FileBrowserViewModel(mockGetFilesUseCase, mockDownloadFileUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadFiles_success_updatesUiStateToSuccess() = testScope.runTest {
        val files = listOf(
            com.github.cgang.syncfiles.domain.model.FileObject(
                serverId = 1,
                repoName = "test",
                path = "/file.txt",
                name = "file.txt",
                size = 1024,
                isDir = false
            )
        )

        coEvery { mockGetFilesUseCase("test", "/", false) } returns files

        viewModel.loadFiles("test", "/")

        val state = viewModel.uiState.value
        assert(state is com.github.cgang.syncfiles.presentation.files.FileBrowserUiState.Success)
        assert((state as com.github.cgang.syncfiles.presentation.files.FileBrowserUiState.Success).files == files)
    }

    @Test
    fun loadFiles_error_updatesUiStateToError() = testScope.runTest {
        coEvery { mockGetFilesUseCase(any(), any(), any()) } throws IOException("Network error")

        viewModel.loadFiles("test", "/")

        val state = viewModel.uiState.value
        assert(state is com.github.cgang.syncfiles.presentation.files.FileBrowserUiState.Error)
    }
}
