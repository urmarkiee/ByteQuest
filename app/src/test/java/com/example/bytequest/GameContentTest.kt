package com.example.bytequest

import org.junit.Assert.*
import org.junit.Test

class GameContentTest {
    @Test fun challengeCodesStayCompatible() {
        assertEquals("BQ4-LOG-3", GameContent.makeChallenge("LOG", 3)?.code)
        assertEquals(3, GameContent.challengePuzzleIndices(GameContent.parseChallenge(" bq4-log-3 ")!!).size)
        assertEquals(40, GameContent.challengePuzzleIndices(GameContent.parseChallenge("BQ4-ALL-40")!!).size)
        assertNull(GameContent.parseChallenge("BQ4-HWR-4"))
        assertNull(GameContent.parseChallenge("BQ4-FAKE-1"))
        assertNull(GameContent.parseChallenge("BQ4-ALL-0"))
        assertNull(GameContent.parseChallenge("not-a-code"))
    }

    @Test fun topicCountsAndCampaignHaveAllPhases() {
        assertEquals(listOf(13, 7, 6, 3, 5, 3, 3), GameContent.lessons.map {
            GameContent.puzzleIndices(it.id).size
        })
        assertEquals(40, GameContent.puzzleIndices("ALL").size)
        assertEquals(7, GameContent.lessons.size)
        assertEquals(5, GameContent.puzzles.count { it.type == PuzzleType.ORDER })
        assertEquals(3, GameContent.puzzles.count { it.type == PuzzleType.MATCH })
    }

    @Test fun binaryConversionsAreAccurate() {
        assertEquals(5, GameContent.binaryValue(intArrayOf(0, 1, 0, 1)))
        assertEquals(10, GameContent.binaryValue(intArrayOf(1, 0, 1, 0)))
        assertEquals(-1, GameContent.binaryValue(intArrayOf(1, 2, 0, 1)))
    }

    @Test fun logicGatesFollowTruthTable() {
        assertEquals(0, GameContent.gateValue("AND", 1, 0))
        assertEquals(1, GameContent.gateValue("AND", 1, 1))
        assertEquals(1, GameContent.gateValue("OR", 0, 1))
        assertEquals(1, GameContent.gateValue("XOR", 1, 0))
        assertEquals(0, GameContent.gateValue("XOR", 1, 1))
    }

    @Test fun eightOriginalPuzzlesRemainIntact() {
        val puzzles = GameContent.puzzles
        assertTrue(GameContent.check(puzzles[0], intArrayOf(0, 1, 0, 1), 0, 0, -1))
        assertFalse(GameContent.check(puzzles[0], intArrayOf(1, 0, 0, 0), 0, 0, -1))
        assertTrue(GameContent.check(puzzles[2], intArrayOf(0, 0, 0, 0), 1, 1, -1))
        assertTrue(GameContent.check(puzzles[5], intArrayOf(0, 0, 0, 0), 0, 0, 1))
        assertFalse(GameContent.check(puzzles[5], intArrayOf(0, 0, 0, 0), 0, 0, 0))
    }

    @Test fun orderedFlowchartAndCodeBlocks() {
        val blank = intArrayOf(0, 0, 0, 0)
        val flow = GameContent.puzzles[8]
        assertTrue(GameContent.check(flow, blank, 0, 0, -1, listOf(1, 2, 4, 0, 3)))
        assertFalse(GameContent.check(flow, blank, 0, 0, -1, listOf(1, 4, 2, 0, 3)))
        assertFalse(GameContent.check(flow, blank, 0, 0, -1, listOf(1, 2, 4, 4, 3)))
        val code = GameContent.puzzles[9]
        assertTrue(GameContent.check(code, blank, 0, 0, -1, listOf(1, 3, 2, 0)))
        assertTrue(GameContent.check(code, blank, 0, 0, -1, listOf(3, 1, 2, 0)))
        assertFalse(GameContent.check(code, blank, 0, 0, -1, listOf(1, 2, 3, 0)))
    }

    @Test fun hardwareMatchesMustBeOneToOne() {
        val blank = intArrayOf(0, 0, 0, 0)
        val hardware = GameContent.puzzles[10]
        assertTrue(GameContent.check(hardware, blank, 0, 0, -1, matches = listOf(2, 3, 0, 1)))
        assertFalse(GameContent.check(hardware, blank, 0, 0, -1, matches = listOf(2, 3, 0, 0)))
        assertFalse(GameContent.check(hardware, blank, 0, 0, -1, matches = listOf(-1, 3, 0, 1)))
    }

    @Test fun networkStepOrdering() {
        val blank = intArrayOf(0, 0, 0, 0)
        assertTrue(GameContent.check(GameContent.puzzles[11], blank, 0, 0, -1, listOf(2, 1, 3, 0)))
        assertFalse(GameContent.check(GameContent.puzzles[11], blank, 0, 0, -1, listOf(0, 1, 2, 3)))
    }
}
