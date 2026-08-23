package com.lianshan.lslife.stress

import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.core.database.MerchantDao
import com.lianshan.lslife.core.database.MerchantEntity
import com.lianshan.lslife.core.model.MerchantPage
import com.lianshan.lslife.core.network.ApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis

class MockDataStressTest {

    private lateinit var apiService: ApiService
    private lateinit var merchantDao: MerchantDao
    private lateinit var repository: LsRepository

    @Before
    fun setUp() {
        apiService = mockk()
        merchantDao = mockk()
        repository = LsRepository(apiService, merchantDao)
    }

    @Test
    fun `test massive data mapping and memory processing`() = runTest {
        // Generate 50,000 mock merchants
        val massiveList = List(50_000) { index ->
            MerchantEntity(
                id = "m_$index",
                name = "Mock Merchant $index",
                rating = 4.5,
                distance = (index % 100).toDouble(),
                sales = index % 1000,
                avgPrice = 20.0,
                tags = "tag1|tag2",
                deliveryFee = 2.0,
                deliveryTime = 30,
                logo = "mock_url",
                category = "job",
                cachedAt = System.currentTimeMillis()
            )
        }

        // Mock DAO to return the massive list
        coEvery { merchantDao.getAll() } returns massiveList

        // Force fallback to cache (api throws exception or we just query local cache logic)
        coEvery { apiService.merchants(any(), any(), any()) } throws Exception("Network Error")

        val timeTaken = measureTimeMillis {
            val result = repository.merchants(category = "all", q = null, sort = "default")
            assertTrue(result.isSuccess)
            val page = result.getOrNull()
            assertEquals(50_000, page?.list?.size)
        }

        println("===============================================================")
        println("[STRESS TEST 1] 50,000 Records Mapping Time: $timeTaken ms")
        println("===============================================================")
        
        // Ensure mapping is fast enough (e.g. less than 2000ms for 50k items locally)
        assertTrue("Mapping took too long: $timeTaken ms", timeTaken < 3000)
    }

    @Test
    fun `test concurrent data loading`() = runTest {
        // Mock a small list for fast retrieval
        val smallList = List(100) { index ->
            MerchantEntity(
                id = "m_$index",
                name = "Mock Merchant $index",
                rating = 4.5,
                distance = 1.0,
                sales = 100,
                avgPrice = 20.0,
                tags = "",
                deliveryFee = 0.0,
                deliveryTime = 30,
                logo = "",
                category = "all",
                cachedAt = 0L
            )
        }
        coEvery { merchantDao.getAll() } returns smallList
        coEvery { apiService.merchants(any(), any(), any()) } throws Exception("Offline")

        // Launch 1,000 concurrent requests to repository
        val timeTaken = measureTimeMillis {
            val jobs = (1..1000).map {
                async(Dispatchers.Default) {
                    repository.merchants(category = "all", q = null, sort = "default")
                }
            }
            val results = jobs.awaitAll()
            assertEquals(1000, results.size)
            assertTrue(results.all { it.isSuccess })
        }

        println("===============================================================")
        println("[STRESS TEST 2] 1,000 Concurrent Requests Time: $timeTaken ms")
        println("===============================================================")
    }
}
