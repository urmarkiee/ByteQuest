package com.example.bytequest

import org.junit.Assert.*
import org.junit.Test

class QuizBankTest {
    @Test fun exactly350UniqueQuestionsWith50PerTopic() {
        assertEquals(350, QuizBank.questions.size)
        assertEquals(350, QuizBank.questions.map { it.id }.toSet().size)
        assertEquals(350, QuizBank.questions.map { it.prompt }.toSet().size)
        assertEquals(GameContent.lessons.map { it.id }.toSet(), QuizBank.questions.map { it.lessonId }.toSet())
        assertEquals(listOf(50, 50, 50, 50, 50, 50, 50), GameContent.lessons.map { QuizBank.forLesson(it.id).size })
    }
    @Test fun everyQuestionHasFourChoicesAndCurriculumReference() {
        QuizBank.questions.forEach { question ->
            assertEquals(4, question.options.size)
            assertEquals(4, question.options.distinct().size)
            assertTrue(question.correctIndex in 0..3)
            assertTrue(question.explanation.isNotBlank())
            assertTrue(question.prompt.isNotBlank())
            val lesson = GameContent.lessons.first { it.id == question.lessonId }
            assertTrue(question.referencePoint in lesson.keyIdeas.indices)
            assertTrue(QuizBank.lessonReference(question).contains(lesson.keyIdeas[question.referencePoint]))
            assertEquals(question.options[question.correctIndex], QuizBank.correctOption(question))
            assertTrue(QuizBank.studySources[question.lessonId]?.url?.startsWith("https://") == true)
        }
    }
    @Test fun shortTopicPacksArePlayableAndComplete() {
        GameContent.lessons.forEach { lesson ->
            val ids = (0..4).flatMap { QuizBank.tenQuestionPack(lesson.id, it) }.map { it.id }
            assertEquals(50, ids.size)
            assertEquals(50, ids.toSet().size)
            assertEquals(QuizBank.forLesson(lesson.id).map { it.id }, ids)
        }
    }
    @Test fun classroomCodesAndSavedLegacyQuestionsStayCompatible() {
        assertEquals("BQQ-ALL-350", QuizBank.makeQuizChallenge("ALL", 350)?.code)
        assertEquals(350, QuizBank.challengeQuestionIds(QuizBank.parseQuizChallenge("bqq-all-350")!!).size)
        assertEquals(50, QuizBank.challengeQuestionIds(QuizBank.parseQuizChallenge("BQQ-NUM-50")!!).size)
        assertEquals(50, QuizBank.challengeQuestionIds(QuizBank.parseQuizChallenge("BQQ-ALL-50")!!).size)
        assertEquals(50, QuizBank.questions.take(50).count { it.id.takeLast(2).toInt() <= 8 }) // old bank is first
        assertNull(QuizBank.parseQuizChallenge("BQQ-LOG-51"))
        assertNull(QuizBank.parseQuizChallenge("BQQ-ALL-351"))
        assertNull(QuizBank.parseQuizChallenge("BQQ-FAKE-3"))
        assertEquals("BQ4-LOG-3", GameContent.makeChallenge("LOG", 3)?.code)
        assertEquals(40, GameContent.puzzles.size)
    }
}
