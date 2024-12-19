package com.dicoding.storyapp.view.story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.storyapp.data.model.ListStoryItem
import com.dicoding.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class StoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var viewModel: StoryViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = StoryViewModel(storyRepository, mock(StoryPagingSource::class.java))
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `getStories success - returns valid data`() = runTest {
        val dummyPagingData = DataDummy.generateDummyPagingData()
        val token = "dummy_token"
        `when`(storyRepository.getStories(token)).thenReturn(flowOf(dummyPagingData))
        val differ = createAsyncPagingDataDiffer()
        viewModel.getStories(token)
        viewModel.pagingData.observeForever { pagingData ->
            testScope.launch {
                differ.submitData(pagingData)
            }
        }
        advanceUntilIdle()
        assert(differ.snapshot().items.isNotEmpty())
        assert(differ.itemCount == DataDummy.DUMMY_STORY_SIZE)

        val firstItem = differ.snapshot().items[0]
        val expectedFirstItem = ListStoryItem(
            id = "1",
            name = "Story 1",
            description = "Description 1",
            photoUrl = "https://example.com/story1.jpg",
        )
        assert(firstItem == expectedFirstItem)
    }


    @Test
    fun `getStories empty - returns empty data`() = runTest {
        val emptyPagingData = PagingData.empty<ListStoryItem>()
        val token = "dummy_token"
        `when`(storyRepository.getStories(token)).thenReturn(flowOf(emptyPagingData))
        val differ = createAsyncPagingDataDiffer()
        viewModel.getStories(token)
        viewModel.pagingData.observeForever { pagingData ->
            testScope.launch {
                differ.submitData(pagingData)
            }
        }

        advanceUntilIdle()

        assert(differ.itemCount == 0)
    }


    private fun createAsyncPagingDataDiffer(): AsyncPagingDataDiffer<ListStoryItem> {
        return AsyncPagingDataDiffer(
            diffCallback = object : DiffUtil.ItemCallback<ListStoryItem>() {
                override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                    return oldItem == newItem
                }
            },
            updateCallback = NoopListUpdateCallback(),
            mainDispatcher = Dispatchers.Main,
            workerDispatcher = Dispatchers.IO
        )
    }

    class NoopListUpdateCallback : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) = Unit
        override fun onRemoved(position: Int, count: Int) = Unit
        override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
        override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
