package com.kotliny.network.engine

import com.kotliny.network.engine.core.clockTime
import com.kotliny.network.engine.core.epochTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeTest {

    @Test
    fun `clock time always returns different values`() {
        val list = buildList { add(repeat(100) { clockTime() }) }
        assertEquals(list.size, list.distinct().size)
    }

    @Test
    fun `epoch time is ok`() {
        epochTime()
    }
}
