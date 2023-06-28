package com.kotliny.network.engine

import com.kotliny.network.engine.core.Date
import com.kotliny.network.engine.core.DateFormats
import com.kotliny.network.engine.core.epochTime
import com.kotliny.network.engine.test.utils.Randoms
import kotlin.test.*

class DateTest {

    @Test
    fun `check empty constructor`() {
        val date = Date()
        val ts = date.epoch()
        assertTrue(ts - epochTime() < 1000)
    }

    @Test
    fun `check timestamp constructor`() {
        val ts = epochTime()
        val date = Date(ts)
        val ts2 = date.epoch()
        assertEquals(ts, ts2)
    }

    @Test
    fun `check date constructor`() {
        val date = Date(2010, 2, 4)
        val ts = date.epoch()
        assertEquals(1265241600000L, ts)
    }

    @Test
    fun `check date constructor full`() {
        val date = Date(2010, 2, 4, 13, 54, 43)
        val ts = date.epoch()
        assertEquals(1265291683000L, ts)
    }

    @Test
    fun `check parser`() {
        val original = "Tue, 02 May 2023 15:24:48 GMT"
        val parsed = Date.parseOrNull(original, DateFormats.standard)!!
        val result = parsed.print(DateFormats.standard)
        assertEquals(original, result)
    }

    @Test
    fun `check parser2`() {
        val original = "Tue, 23 May 2023 21:31:58 GMT"
        val parsed = Date.parseOrNull(original, DateFormats.standard)!!
        val result = parsed.print(DateFormats.standard)
        assertEquals(original, result)
    }

    @Test
    fun `check parser with compatibility date`() {
        val original = "Tue, 23-May-2023 21:31:58 GMT"
        val parsed = DateFormats.parseOrNull(original)!!
        assertEquals(1684877518000L, parsed.epoch())
    }

    @Test
    fun `check parser with wrong date`() {
        val original = "Tue, 2023 21:31:58 GMT"
        assertNull(DateFormats.parseOrNull(original))
    }

    @Test
    fun `check parser wrong`() {
        val original = "Tue, 02 May 2023 15:24:48 GMT"
        val parsed = Date.parseOrNull(original, DateFormats.pretty)
        assertNull(parsed)
    }

    @Test
    fun `check equals`() {
        val date1 = Date(2010, 2, 4)
        val date2 = Date(1265241600000L)
        assertEquals(date1, date2)
        assertEquals(date1.toString(), date2.toString())
    }

    @Test
    fun `check equals 2`() {
        val date1 = Date(2023, 5, 23, 21, 31, 58)
        val date2 = Date.parseOrNull("Tue, 23 May 2023 21:31:58 GMT", DateFormats.standard)!!
        assertEquals(date1.toString(), date2.toString())
        assertEquals(date1, date2)
    }

    @Test
    fun `check not equals`() {
        val date1 = Date(Randoms.int(1000, 10000000).toLong())
        val date2 = Date(Randoms.int(1000, 10000000).toLong())

        assertFalse(date1.equals("bad"))
        assertNotEquals(date1, date2)
        assertNotEquals(date1.toString(), date2.toString())
    }

    @Test
    fun `check compare`() {
        val date1 = Date(2010, 2, 4)
        val date2 = Date()
        assertTrue(date2 > date1)
    }

    @Test
    fun `check plus`() {
        val days = Randoms.int(1, 50)
        val date = Date(2010, 2, 4)
        val dateResult = date + 3600 * 24 * days
        val dateExpected = Date(2010, 2, 4 + days)
        assertEquals(dateExpected.toString(), dateResult.toString())
    }

    @Test
    fun `check minus`() {
        val days = Randoms.int(1, 50)
        val date = Date(2010, 2, 20)
        val dateResult = date - 3600 * 24 * days
        val dateExpected = Date(2010, 2, 20 - days)
        assertEquals(dateExpected.toString(), dateResult.toString())
    }

    @Test
    fun `check plus minus`() {
        val days = Randoms.int(1, 50)
        val date = Date(2010, 2, 20)
        val dateResult = date + days
        val dateResult2 = dateResult - days
        assertEquals(date.toString(), dateResult2.toString())
    }
}