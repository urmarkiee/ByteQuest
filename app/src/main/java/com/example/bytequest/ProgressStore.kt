package com.example.bytequest

import android.content.Context
import android.content.SharedPreferences

/** All data stays on this device. No account, class roster, or network required. */
class ProgressStore(context: Context) {
    // MainActivity's previous getPreferences(MODE_PRIVATE) uses this exact name.
    private val prefs: SharedPreferences = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE)

    val campaignIndex: Int get() = prefs.getInt("campaign_index", prefs.getInt("next_level", 0))
        .coerceIn(0, GameContent.puzzles.size)
    val campaignPoints: Int get() = prefs.getInt("campaign_points", prefs.getInt("score", 0))
        .coerceAtLeast(0)

    fun finishCampaignLevel(index: Int) {
        // Do not allow repeated scoring by replaying older levels.
        if (index != campaignIndex) return
        prefs.edit()
            .putInt("campaign_index", (index + 1).coerceAtMost(GameContent.puzzles.size))
            .putInt("campaign_points", campaignPoints + 10)
            .apply()
    }

    fun attempts(topic: String): Int = prefs.getInt("topic.$topic.attempts", 0)
    fun correct(topic: String): Int = prefs.getInt("topic.$topic.correct", 0)
    fun hints(topic: String): Int = prefs.getInt("topic.$topic.hints", 0)
    fun flawless(topic: String): Int = prefs.getInt("topic.$topic.flawless", 0)
    fun learned(topic: String): Boolean = prefs.getBoolean("lesson.$topic.viewed", false)
    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound.enabled", true)
        set(value) { prefs.edit().putBoolean("sound.enabled", value).apply() }
    val classroomCompleted: Int get() = prefs.getInt("classroom.completed", 0)
    val lastClassCode: String get() = prefs.getString("classroom.last_code", "") ?: ""
    val lastClassScore: Int get() = prefs.getInt("classroom.last_score", 0)
    val lastClassPossible: Int get() = prefs.getInt("classroom.last_possible", 0)

    fun markLesson(topic: String) {
        prefs.edit().putBoolean("lesson.$topic.viewed", true).apply()
    }
    fun recordAttempt(topic: String, wasCorrect: Boolean, wasFlawless: Boolean) {
        prefs.edit()
            .putInt("topic.$topic.attempts", attempts(topic) + 1)
            .putInt("topic.$topic.correct", correct(topic) + if (wasCorrect) 1 else 0)
            .putInt("topic.$topic.flawless", flawless(topic) + if (wasCorrect && wasFlawless) 1 else 0)
            .apply()
    }
    fun recordHint(topic: String) {
        prefs.edit().putInt("topic.$topic.hints", hints(topic) + 1).apply()
    }
    fun recordClassroomResult(code: String, score: Int, possible: Int) {
        prefs.edit()
            .putInt("classroom.completed", classroomCompleted + 1)
            .putString("classroom.last_code", code)
            .putInt("classroom.last_score", score)
            .putInt("classroom.last_possible", possible)
            .apply()
    }

    /** Quiz results are tracked separately from the 12 original puzzle-level statistics. */
    fun quizSeen(id: String): Boolean = prefs.getBoolean("quiz.$id.seen", false)
    fun quizMastered(id: String): Boolean = prefs.getBoolean("quiz.$id.mastered", false)
    fun quizAttempts(id: String): Int = prefs.getInt("quiz.$id.attempts", 0)
    fun quizCorrect(id: String): Int = prefs.getInt("quiz.$id.correct", 0)

    fun recordQuizAnswer(question: QuizQuestion, correctAnswer: Boolean) {
        prefs.edit()
            .putBoolean("quiz.${question.id}.seen", true)
            .putBoolean("quiz.${question.id}.mastered", quizMastered(question.id) || correctAnswer)
            .putInt("quiz.${question.id}.attempts", quizAttempts(question.id) + 1)
            .putInt("quiz.${question.id}.correct", quizCorrect(question.id) + if (correctAnswer) 1 else 0)
            .apply()
    }

    fun quizAnsweredCount(topicId: String = "ALL"): Int = QuizBank.forLesson(topicId).count { quizSeen(it.id) }
    fun quizMasteredCount(topicId: String = "ALL"): Int = QuizBank.forLesson(topicId).count { quizMastered(it.id) }
    fun quizTopicAttempts(topicId: String = "ALL"): Int = QuizBank.forLesson(topicId).sumOf { quizAttempts(it.id) }
    fun quizTopicCorrect(topicId: String = "ALL"): Int = QuizBank.forLesson(topicId).sumOf { quizCorrect(it.id) }

    fun unlockedBadges(): Set<String> {
        val topicsSolved = GameContent.lessons.count { correct(it.id) > 0 }
        return buildSet {
            if (campaignIndex > 0 || GameContent.lessons.any { correct(it.id) > 0 }) add("FIRST")
            if (GameContent.lessons.count { learned(it.id) } >= 3) add("READER")
            if (correct("NUM") >= 2) add("BINARY")
            if (correct("LOG") >= 3) add("LOGIC")
            if (GameContent.lessons.sumOf { flawless(it.id) } >= 3) add("FLAWLESS")
            if (topicsSolved == GameContent.lessons.size) add("ALLROUNDER")
            if (campaignIndex == GameContent.puzzles.size) add("GRADUATE")
            if (classroomCompleted > 0) add("CLASSROOM")
            if (quizAnsweredCount() >= 1) add("QUIZ_FIRST")
            if (quizAnsweredCount() >= 25) add("QUIZ_HALF")
            if (quizAnsweredCount() >= 50) add("QUIZ_50")
            if (GameContent.lessons.any { quizAnsweredCount(it.id) >= 50 }) add("QUIZ_TOPIC")
            if (quizAnsweredCount() >= 175) add("QUIZ_175")
            if (quizAnsweredCount() == QuizBank.questions.size) add("QUIZ_350")
            if (quizMasteredCount() == QuizBank.questions.size) add("QUIZ_MASTER")
        }
    }

    fun resetAll() = prefs.edit().clear().apply()
}
