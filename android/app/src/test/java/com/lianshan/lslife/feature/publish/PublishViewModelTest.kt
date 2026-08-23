package com.lianshan.lslife.feature.publish

import android.content.Context
import com.lianshan.lslife.core.data.LsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PublishViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit val repo: LsRepository
    private lateinit val context: Context
    private lateinit val viewModel: PublishViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        context = mockk(relaxed = true)
        viewModel = PublishViewModel(repo, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generateAiDescription updates fields on success`() = runTest(testDispatcher) {
        viewModel.onTitle("二手单车")
        
        val aiResponse = mapOf(
            "title" to "捷安特二手单车",
            "description" to "八成新",
            "brand" to "捷安特",
            "parameters" to "21速"
        )
        coEvery { repo.aiGenerateDescription(any(), any(), any()) } returns Result.success(aiResponse)

        viewModel.generateAiDescription()
        
        testScheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("捷安特二手单车", state.title)
        assertEquals("八成新", state.description)
        assertEquals("捷安特", state.brand)
        assertEquals("21速", state.parameters)
    }
}
