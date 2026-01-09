package org.example.app

import org.example.utilities.StringUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class test_StringUtils {

    @Test
    fun `split ignores extra spaces and join joins with single spaces`() {
        val list = StringUtils.split("a  b   c")
        assertEquals(3, list.size())
        assertEquals("a", list.get(0))
        assertEquals("b", list.get(1))
        assertEquals("c", list.get(2))

        val joined = StringUtils.join(list)
        assertEquals("a b c", joined)
    }

    @Test
    fun `split on blank string returns empty list`() {
        val list = StringUtils.split("")
        assertEquals(0, list.size())
    }
}
