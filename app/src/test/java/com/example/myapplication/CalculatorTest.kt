package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*

class CalculatorTest {

    @Test
    fun testBasicMath() {
        assertEquals("9", Solver.solve("√81"))
        assertEquals("56", Solver.solve("8×7"))
        assertEquals("10", Solver.solve("2+2*4"))
        assertEquals("1", Solver.solve("sin(0)")) // Solver zatím sin nemá, ale pro budoucí rozšíření
    }

    @Test
    fun testSimpleEquations() {
        assertEquals("x = 5", Solver.solve("2x=10"))
        assertEquals("a = 2", Solver.solve("a+5=7"))
    }

    @Test
    fun testVariableRelations() {
        // Test 9a=5x -> a=0.5556x, x=1.8a
        val res = Solver.solve("9a=5x")
        assertTrue(res.contains("a = 0.5556x"))
        assertTrue(res.contains("x = 1.8a"))
    }

    @Test
    fun testSystems() {
        val res = Solver.solve("x+y=10; x-y=2")
        assertTrue(res.contains("x = 6"))
        assertTrue(res.contains("y = 4"))
    }

    @Test
    fun testErrors() {
        assertEquals("Nelze vypočítat", Solver.solve("1=2"))
        assertEquals("Nelze vypočítat", Solver.solve("2/0"))
    }
}
