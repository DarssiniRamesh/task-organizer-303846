package org.example.app.testutil

import androidx.lifecycle.LiveData
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Await a LiveData emission in a unit test.
 */
fun <T> LiveData<T>.getOrAwaitValue(
    timeout: Long = 2,
    unit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)

    lateinit var observer: androidx.lifecycle.Observer<T>
    observer = androidx.lifecycle.Observer { value ->
        data = value
        latch.countDown()
        // Unregister after first value to avoid leaking observers across tests.
        this.removeObserver(observer)
    }

    // Register observer on the calling thread; InstantTaskExecutorRule makes it synchronous.
    observeForever(observer)

    if (!latch.await(timeout, unit)) {
        removeObserver(observer)
        throw TimeoutException("LiveData value was never set within $timeout $unit.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}
