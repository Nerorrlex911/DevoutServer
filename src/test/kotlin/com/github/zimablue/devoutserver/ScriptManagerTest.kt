package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.script.ScriptManagerImpl
import org.junit.jupiter.api.BeforeAll
import taboolib.common5.cint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScriptManagerTest {

    @Test
    fun `test script loading`() {
        assertEquals(3,ScriptManagerImpl.compiledScripts.size,"ScriptManager should load 2 scripts: ${ScriptManagerImpl.compiledScripts}")
        assertTrue(ScriptManagerImpl.compiledScripts.containsKey("F:\\Code\\MyCode\\MineStom\\Devout\\DevoutServer\\test\\scripts\\test.js"), "Script 'test.js' should be loaded")
    }

    @Test
    fun `test script function invocation`() {
        val result = ScriptManagerImpl.run("test.js", "test")
        assertEquals(1, result.cint, "Script function 'test.js' should be invoked successfully")
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup(): Unit {
            // Setup test environment
            DevoutServer.start()
        }
    }
}