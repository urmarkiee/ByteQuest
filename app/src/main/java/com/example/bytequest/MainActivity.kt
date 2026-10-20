package com.example.bytequest

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.FrameLayout
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.Gravity
import android.view.DragEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

/** One-activity UI, no backend or login. Works offline on API 24+. */
class MainActivity : Activity() {
    private enum class Tab { HOME, LEVELS, PLAY, LEARN, QUIZ, PROGRESS, BADGES, CLASS, PROFILE, SETTINGS, MORE }
    private enum class PlayMode { CAMPAIGN, PRACTICE, CLASSROOM, REPLAY }

    private val bg = Color.parseColor("#030713")
    private val panel = Color.parseColor("#091A38")
    private val muted = Color.parseColor("#B5C9E6")
    private val light = Color.WHITE
    private val cyan = Color.parseColor("#04D9FF")
    private val green = Color.parseColor("#28F4A5")
    private val amber = Color.parseColor("#FFC345")
    private val red = Color.parseColor("#FF6389")

    private lateinit var store: ProgressStore
    private lateinit var content: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var tabButtons: List<LinearLayout>
    private val navTabs = listOf(Tab.HOME, Tab.LEVELS, Tab.LEARN, Tab.QUIZ, Tab.MORE)

    private var tab = Tab.PLAY
    private var mode = PlayMode.CAMPAIGN
    private var activeLesson: String? = null
    private var roundIndices: List<Int> = emptyList()
    private var roundPosition = 0
    private var challengeCode = ""
    private var challengeScore = 0
    private var challengeFinished = false

    // Student question-bank session, independent of the story and classroom campaign.
    private var quizIds: List<String> = emptyList()
    private var quizPosition = 0
    private var quizSelected = -1
    private var quizSubmitted = false
    private var quizSessionCorrect = 0
    private var quizCompleted = false
    private var quizClassCode = ""

    // Current puzzle input and feedback are retained when switching screens.
    private val bits = IntArray(4)
    private var inputA = 0
    private var inputB = 0
    private var selectedAnswer = -1
    private var order: MutableList<Int> = mutableListOf()
    private var matches: MutableList<Int> = mutableListOf()
    private var selectedOrderPosition = -1
    private var selectedComponent = -1
    private var wrongAttempts = 0
    private var hintUsed = false
    private var solved = false
    private var feedback = ""
    private var feedbackColor = muted
    private var subRoundPosition = 0
    private var valueLabel: TextView? = null
    private var feedbackLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        store = ProgressStore(this)
        content = findViewById(R.id.contentHost)
        scroll = findViewById(R.id.contentScroll)
        tabButtons = listOf(
            findViewById(R.id.tabHome), findViewById(R.id.tabLevels),
            findViewById(R.id.tabLearn), findViewById(R.id.tabQuiz),
            findViewById(R.id.tabMore)
        )
        tabButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (navTabs[index] == Tab.LEARN) activeLesson = null
                selectTab(navTabs[index])
            }
        }
        if (savedInstanceState == null) {
            roundIndices = listOf(store.campaignIndex)
            selectTab(Tab.HOME)
        } else {
            mode = PlayMode.valueOf(savedInstanceState.getString("mode", "CAMPAIGN")!!)
            tab = Tab.valueOf(savedInstanceState.getString("tab", "PLAY")!!)
            activeLesson = savedInstanceState.getString("lesson")
            roundIndices = savedInstanceState.getIntegerArrayList("indices")?.toList() ?: emptyList()
            roundPosition = savedInstanceState.getInt("position")
            challengeCode = savedInstanceState.getString("challengeCode", "") ?: ""
            challengeScore = savedInstanceState.getInt("challengeScore")
            challengeFinished = savedInstanceState.getBoolean("challengeFinished")
            quizIds = savedInstanceState.getStringArrayList("quizIds")?.toList() ?: emptyList()
            quizPosition = savedInstanceState.getInt("quizPosition")
            quizSelected = savedInstanceState.getInt("quizSelected", -1)
            quizSubmitted = savedInstanceState.getBoolean("quizSubmitted")
            quizSessionCorrect = savedInstanceState.getInt("quizSessionCorrect")
            quizCompleted = savedInstanceState.getBoolean("quizCompleted")
            quizClassCode = savedInstanceState.getString("quizClassCode", "") ?: ""
            val previousBits = savedInstanceState.getIntArray("bits")
            if (previousBits?.size == 4) previousBits.copyInto(bits)
            inputA = savedInstanceState.getInt("a")
            inputB = savedInstanceState.getInt("b")
            selectedAnswer = savedInstanceState.getInt("answer", -1)
            order = savedInstanceState.getIntegerArrayList("order")?.toMutableList() ?: mutableListOf()
            matches = savedInstanceState.getIntegerArrayList("matches")?.toMutableList() ?: mutableListOf()
            selectedOrderPosition = savedInstanceState.getInt("selectedOrderPosition", -1)
            selectedComponent = savedInstanceState.getInt("selectedComponent", -1)
            wrongAttempts = savedInstanceState.getInt("wrong")
            hintUsed = savedInstanceState.getBoolean("hint")
            solved = savedInstanceState.getBoolean("solved")
            feedback = savedInstanceState.getString("feedback", "") ?: ""
            feedbackColor = savedInstanceState.getInt("feedbackColor", muted)
            subRoundPosition = savedInstanceState.getInt("subRoundPosition", 0)
            if (roundIndices.isEmpty() && mode == PlayMode.CAMPAIGN) {
                roundIndices = listOf(store.campaignIndex)
            }
            selectTab(tab)
        }
    }

    @Deprecated("Android system back navigation")
    override fun onBackPressed() {
        when(tab) {
            Tab.HOME -> super.onBackPressed()
            Tab.LEARN -> {if(activeLesson!=null){activeLesson=null;selectTab(Tab.LEARN)}else selectTab(Tab.HOME)}
            Tab.QUIZ -> {
                if(quizIds.isNotEmpty()){
                    AlertDialog.Builder(this).setTitle("Exit quiz?")
                        .setMessage("Submitted answers are saved. Your current session will end.")
                        .setNegativeButton("Stay",null)
                        .setPositiveButton("Exit"){_,_->quizIds=emptyList();quizClassCode="";quizCompleted=false;selectTab(Tab.QUIZ)}.show()
                }else selectTab(Tab.HOME)
            }
            else -> selectTab(Tab.HOME)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("mode", mode.name)
        outState.putString("tab", tab.name)
        outState.putString("lesson", activeLesson)
        outState.putIntegerArrayList("indices", ArrayList(roundIndices))
        outState.putInt("position", roundPosition)
        outState.putString("challengeCode", challengeCode)
        outState.putInt("challengeScore", challengeScore)
        outState.putBoolean("challengeFinished", challengeFinished)
        outState.putStringArrayList("quizIds", ArrayList(quizIds))
        outState.putInt("quizPosition", quizPosition)
        outState.putInt("quizSelected", quizSelected)
        outState.putBoolean("quizSubmitted", quizSubmitted)
        outState.putInt("quizSessionCorrect", quizSessionCorrect)
        outState.putBoolean("quizCompleted", quizCompleted)
        outState.putString("quizClassCode", quizClassCode)
        outState.putIntArray("bits", bits)
        outState.putInt("a", inputA)
        outState.putInt("b", inputB)
        outState.putInt("answer", selectedAnswer)
        outState.putIntegerArrayList("order", ArrayList(order))
        outState.putIntegerArrayList("matches", ArrayList(matches))
        outState.putInt("selectedOrderPosition", selectedOrderPosition)
        outState.putInt("selectedComponent", selectedComponent)
        outState.putInt("wrong", wrongAttempts)
        outState.putBoolean("hint", hintUsed)
        outState.putBoolean("solved", solved)
        outState.putString("feedback", feedback)
        outState.putInt("feedbackColor", feedbackColor)
        outState.putInt("subRoundPosition", subRoundPosition)
        super.onSaveInstanceState(outState)
    }

    private fun selectTab(newTab: Tab) {
        val screenChanged = tab != newTab
        tab = newTab
        tabButtons.forEachIndexed { i, button ->
            val selected = navTabs[i] == tab ||
                (navTabs[i] == Tab.MORE && tab in listOf(Tab.PROGRESS, Tab.BADGES, Tab.CLASS, Tab.PROFILE, Tab.SETTINGS)) ||
                (navTabs[i] == Tab.LEVELS && tab == Tab.PLAY)
            button.setBackgroundResource(if (selected) R.drawable.nav_active else R.drawable.nav_inactive)
            val icon = button.getChildAt(0) as ImageView
            val label = button.getChildAt(1) as TextView
            val marker = button.getChildAt(2)
            icon.setColorFilter(if (selected) cyan else Color.parseColor("#B3CAE9"))
            label.setTextColor(if (selected) cyan else Color.parseColor("#B3CAE9"))
            marker.visibility = if (selected) View.VISIBLE else View.INVISIBLE
        }
        content.removeAllViews()
        feedbackLabel = null
        valueLabel = null
        when (tab) {
            Tab.HOME -> renderHome()
            Tab.LEVELS -> renderLevels()
            Tab.PLAY -> renderPlay()
            Tab.LEARN -> renderLearn()
            Tab.QUIZ -> renderQuiz()
            Tab.PROGRESS -> renderProgress()
            Tab.BADGES -> renderBadges()
            Tab.CLASS -> renderClass()
            Tab.PROFILE -> renderProfile()
            Tab.SETTINGS -> renderSettings()
            Tab.MORE -> renderMore()
        }
        content.animate().cancel()
        if (screenChanged) {
            content.alpha = 0f
            content.translationY = dp(9).toFloat()
            content.animate().alpha(1f).translationY(0f).setDuration(180).start()
            scroll.post { scroll.scrollTo(0, 0) }
        } else {
            content.alpha = 1f
            content.translationY = 0f
        }
    }

    private fun resetPuzzleInputs() {
        bits.fill(0)
        inputA = 0
        inputB = 0
        selectedAnswer = -1
        order = mutableListOf()
        matches = mutableListOf()
        selectedOrderPosition = -1
        selectedComponent = -1
        wrongAttempts = 0
        hintUsed = false
        solved = false
        feedback = ""
        feedbackColor = muted
    }

    private fun startCampaign() {
        mode = PlayMode.CAMPAIGN
        roundIndices = listOf(store.campaignIndex)
        roundPosition = 0
        subRoundPosition = 0
        challengeFinished = false
        resetPuzzleInputs()
        selectTab(Tab.PLAY)
    }

    private fun replayLevel(index: Int) {
        if (index !in 0 until store.campaignIndex) return
        mode = PlayMode.REPLAY
        roundIndices = listOf(index)
        roundPosition = 0
        subRoundPosition = 0
        resetPuzzleInputs()
        selectTab(Tab.PLAY)
    }

    private fun startPractice(topicId: String) {
        mode = PlayMode.PRACTICE
        roundIndices = GameContent.puzzleIndices(topicId)
        roundPosition = 0
        subRoundPosition = 0
        challengeFinished = false
        resetPuzzleInputs()
        selectTab(Tab.PLAY)
    }

    private fun startClassroom(challenge: Challenge) {
        mode = PlayMode.CLASSROOM
        roundIndices = GameContent.challengePuzzleIndices(challenge)
        roundPosition = 0
        subRoundPosition = 0
        challengeCode = challenge.code
        challengeScore = 0
        challengeFinished = false
        resetPuzzleInputs()
        selectTab(Tab.PLAY)
    }

    private fun currentPuzzle(): Puzzle? = roundIndices.getOrNull(roundPosition)?.let { GameContent.puzzles.getOrNull(it) }

    // ----- Phase 2: polished home dashboard and level selection -----
    private fun renderHome() {
        // Reference screen 2: brand header, student strip, featured learning and 3x2 menu.
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val avatar = ImageView(this).apply {
            setImageResource(R.drawable.student_avatar)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = pillShape(Color.parseColor("#102B5B"), cyan, 100)
            clipToOutline = true
            setPadding(dp(2), dp(2), dp(2), dp(2))
        }
        header.addView(avatar, LinearLayout.LayoutParams(dp(56), dp(56)))
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(13), 0, 0, 0) }
        line(brand, "BYTEQUEST", 27f, cyan, bold = true)
        line(brand, "Computer Studies Adventure", 11f, light, top = 1)
        line(brand, "Made by MR.DEV", 11f, muted, top = 2)
        header.addView(brand, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        content.addView(header, margins(top = 3, bottom = 16))
        val summary = colorCard(Color.parseColor("#0E244B"), Color.parseColor("#3472C4"), 11)
        val info = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        line(left, "👨🏻‍💻  Student", 17f, light, bold = true)
        line(left, "${store.campaignIndex.coerceAtMost(GameContent.puzzles.size)} / ${GameContent.puzzles.size} levels completed", 13f, muted, top = 5)
        info.addView(left, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        line(info, "🏆 ${store.campaignPoints} XP", 15f, amber, bold = true)
        summary.addView(info)
        val feat = colorCard(Color.parseColor("#30103F"), Color.parseColor("#B63BDF"), 15)
        line(feat, "CONTINUE LEARNING  ✦", 12f, Color.parseColor("#F9B2FF"), bold = true)
        val next = GameContent.puzzles.getOrNull(store.campaignIndex)
        line(feat, next?.title ?: "All levels cleared!", 20f, light, bold = true, top = 5)
        line(feat, "${store.campaignIndex} / ${GameContent.puzzles.size} levels", 12f, Color.parseColor("#EFC9FF"), top = 5)
        progressLine(feat, store.campaignIndex, GameContent.puzzles.size, cyan)
        action(feat, if (next == null) "View completion  →" else "Continue adventure   →", "primary") { startCampaign() }
        line(content, "YOUR ADVENTURE", 15f, light, bold = true, top = 21)
        tileRow(
            Triple("🎮", "Play Levels", Color.parseColor("#006CCB")) { selectTab(Tab.LEVELS) },
            Triple("📖", "Lessons", Color.parseColor("#006E73")) { activeLesson=null;selectTab(Tab.LEARN) },
            Triple("🏆", "Quiz Challenge", Color.parseColor("#91451B")) { selectTab(Tab.QUIZ) }
        )
        tileRow(
            Triple("🥇", "Achievements", Color.parseColor("#8C371B")) { selectTab(Tab.BADGES) },
            Triple("▥", "Reports", Color.parseColor("#047E56")) { selectTab(Tab.PROGRESS) },
            Triple("👥", "Classroom", Color.parseColor("#51249C")) { selectTab(Tab.CLASS) }
        )
        line(content, "350 QUESTIONS  •  7 SUBJECTS  •  MADE BY MR.DEV", 11f, muted, top = 19).gravity = Gravity.CENTER_HORIZONTAL
    }

    private data class Triple(val icon: String, val name: String, val color: Int, val onClick: () -> Unit)
    private fun tileRow(vararg tiles: Triple) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tiles.forEachIndexed { i, tile ->
            val holder = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(12), dp(4), dp(12))
                background = pillShape(tile.color, tile.color, 14)
                isClickable = true; isFocusable = true
                setOnClickListener { tile.onClick() }
            }
            val symbol = line(holder, tile.icon, 28f, light)
            symbol.gravity = Gravity.CENTER
            val title = line(holder, tile.name, 11f, light, bold = true, top = 9)
            title.gravity = Gravity.CENTER
            title.minHeight = dp(30)
            row.addView(holder, LinearLayout.LayoutParams(0, dp(116), 1f).apply {
                if (i > 0) marginStart = dp(8)
            })
        }
        content.addView(row, margins(top = 9))
    }

    private fun colorCard(fill: Int, border: Int, top: Int = 12): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(15), dp(14), dp(15), dp(14))
        background = pillShape(fill, border, 16)
        content.addView(this, margins(top = top))
    }

    private fun pillShape(fill: Int, border: Int, radius: Int = 16) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(1), border)
    }

    private fun progressLine(parent: LinearLayout, count: Int, total: Int, tint: Int) {
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = total.coerceAtLeast(1); this.progress = count.coerceIn(0,max)
            progressTintList = ColorStateList.valueOf(tint)
            progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#11305B"))
            minimumHeight = dp(9)
        }
        parent.addView(progress, margins(top = 10))
    }

    private fun pageTitle(title: String, subtitle: String, accent: Int = cyan, back: Tab? = Tab.HOME) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        if (back != null) {
            val arrow = TextView(this).apply {
                text = "‹"; textSize = 32f; setTextColor(bg); gravity = Gravity.CENTER
                background = pillShape(cyan, cyan, 100)
                setOnClickListener {
                    when {
                        back == Tab.LEARN && activeLesson != null -> {activeLesson=null;selectTab(Tab.LEARN)}
                        back == Tab.QUIZ && quizIds.isNotEmpty() -> {
                            AlertDialog.Builder(this@MainActivity).setTitle("Exit current quiz?")
                                .setMessage("Submitted answers are saved. Your current question session will end.")
                                .setNegativeButton("Keep playing", null)
                                .setPositiveButton("Exit") { _, _ ->
                                    quizIds=emptyList();quizClassCode="";quizCompleted=false;selectTab(Tab.QUIZ)
                                }.show()
                        }
                        else -> selectTab(back)
                    }
                }
                contentDescription = "Back"
            }
            row.addView(arrow, LinearLayout.LayoutParams(dp(38), dp(38)))
        }
        val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0) }
        line(labels, title, 22f, accent, bold = true)
        line(labels, subtitle, 12f, muted, top = 3)
        row.addView(labels, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        content.addView(row, margins(top = 4, bottom = 17))
    }

    private fun topicColor(i: Int): Int = listOf(
        Color.parseColor("#00D9FF"), Color.parseColor("#0CE3C0"),
        Color.parseColor("#A86BFF"), Color.parseColor("#FF528F"),
        Color.parseColor("#16BAFF"), Color.parseColor("#FFB82E"),
        Color.parseColor("#7BE45E")
    )[i % 7]

    private fun topicSymbol(i: Int): String = listOf("01", "∧∨", "</>", "🐞", "◉", "↳", "⚙")[i % 7]

    private fun menuRow(title: String, subtitle: String, symbol: String, accent: Int, onClick: () -> Unit) {
        val row = colorCard(Color.parseColor("#081D3D"), Color.parseColor("#1556A3"), 7)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(10), dp(11), dp(10), dp(11))
        val badge = TextView(this).apply {
            text = symbol; textSize = 17f; gravity = Gravity.CENTER; setTextColor(light); typeface = Typeface.DEFAULT_BOLD
            background = pillShape(Color.parseColor("#0F3457"), accent, 9)
        }
        row.addView(badge, LinearLayout.LayoutParams(dp(44), dp(44)))
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0) }
        line(inner,title,16f,light,bold=true)
        line(inner,subtitle,12f,muted,top=3)
        row.addView(inner,LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f))
        line(row,"›",27f,accent,bold=true)
        row.isClickable=true;row.isFocusable=true;row.setOnClickListener{onClick()}
    }

    private fun renderMore() {
        pageTitle("STUDENT HUB", "Everything you need in one place", Color.parseColor("#CDA6FF"), null)
        menuRow("My Profile", "Student progress and game identity", "☺", cyan) { selectTab(Tab.PROFILE) }
        menuRow("Learning Reports", "Your results across seven subjects", "▥", green) { selectTab(Tab.PROGRESS) }
        menuRow("Achievements", "View all 15 collectible badges", "★", amber) { selectTab(Tab.BADGES) }
        menuRow("Classroom Mode", "Create and join offline challenges", "♧", Color.parseColor("#C48BFF")) { selectTab(Tab.CLASS) }
        menuRow("Settings", "Sound, progress and about ByteQuest", "⚙", cyan) { selectTab(Tab.SETTINGS) }
        line(content, "BYTEQUEST  •  MADE BY MR.DEV", 12f, cyan, bold = true, top = 27).gravity=Gravity.CENTER
    }

    private fun renderProfile() {
        pageTitle("PROFILE", "Student profile and your journey", Color.parseColor("#D68FFF"), Tab.MORE)
        val student = colorCard(Color.parseColor("#0D2248"), Color.parseColor("#246CC2"), 3)
        val row = LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val avatar = ImageView(this).apply {
            setImageResource(R.drawable.student_avatar);scaleType=ImageView.ScaleType.CENTER_CROP
            background=pillShape(Color.parseColor("#143763"),cyan,99);clipToOutline=true
        }
        row.addView(avatar,LinearLayout.LayoutParams(dp(76),dp(76)))
        val info = LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(dp(16),0,0,0)}
        line(info,"Student",23f,light,bold=true)
        line(info,"ByteQuest Learner",14f,muted,top=3)
        line(info,"${store.campaignPoints} XP • ${store.unlockedBadges().size} badges",13f,amber,top=5)
        row.addView(info)
        student.addView(row)
        progressLine(student,store.campaignIndex,GameContent.puzzles.size,cyan)
        menuRow("Quiz Progress", "${store.quizAnsweredCount()} / 350 questions attempted", "✓", green) {selectTab(Tab.PROGRESS)}
        menuRow("My Badges", "${store.unlockedBadges().size} / ${badgeInfo.size} earned", "★", amber) {selectTab(Tab.BADGES)}
        menuRow("Settings", "Change preferences", "⚙", cyan) {selectTab(Tab.SETTINGS)}
        line(content,"CREATED BY MR.DEV",12f,cyan,bold=true,top=25).gravity=Gravity.CENTER
    }

    private fun renderSettings() {
        pageTitle("SETTINGS", "Personalize your learning experience", Color.parseColor("#C48DFF"), Tab.MORE)
        val settings = colorCard(Color.parseColor("#0B1E3E"), Color.parseColor("#1B5EAA"))
        line(settings,"AUDIO & FEEDBACK",13f,cyan,bold=true)
        action(settings,if(store.soundEnabled) "♫ Sound effects    ON" else "♫ Sound effects    OFF","muted"){
            store.soundEnabled=!store.soundEnabled;selectTab(Tab.SETTINGS)
        }
        line(settings,"Progress stays on your device. Offline play is fully supported.",13f,muted,top=12)
        val about = colorCard(Color.parseColor("#0B1E3E"),Color.parseColor("#1B5EAA"))
        line(about,"ABOUT BYTEQUEST",13f,cyan,bold=true)
        line(about,"Computer Studies Adventure",20f,light,bold=true,top=8)
        line(about,"350 educational questions, ${GameContent.puzzles.size} interactive game levels, lessons, achievements, and offline classroom challenges.",14f,muted,top=8)
        line(about,"Made by MR.DEV",14f,cyan,bold=true,top=13)
        action(about,"Show welcome screen again","muted"){
            getSharedPreferences("bytequest_welcome",MODE_PRIVATE).edit().putBoolean("seen",false).apply()
            startActivity(Intent(this,WelcomeActivity::class.java))
            finish()
        }
        action(content,"Reset all saved progress","danger"){confirmReset()}
    }

    private fun renderLevels() {
        pageTitle("PLAY LEVELS", "Interactive Learning Puzzles", cyan, Tab.HOME)
        GameContent.puzzles.forEachIndexed { i, puzzle ->
            val complete=i<store.campaignIndex
            val ready=i==store.campaignIndex
            val accent=topicColor(i)
            val type = when(puzzle.type) {
                PuzzleType.BINARY -> "Number systems"; PuzzleType.GATE -> "Digital Logic";
                PuzzleType.CHOICE -> "Knowledge Puzzle"; PuzzleType.ORDER -> "Arrange the code";
                PuzzleType.MATCH -> "Hardware Lab"
            }
            val symbol = if(complete) "✓" else "${i+1}"
            menuRow(puzzle.title,"${GameContent.topicName(puzzle.topicId)} • $type",symbol,accent){
                if(complete) replayLevel(i)
                else if(ready) startCampaign()
                else toast("Complete previous levels to unlock this one.")
            }
        }
        if(store.campaignIndex>=GameContent.puzzles.size)
            action(content,"View completion  →","positive"){startCampaign()}

        // All 350 curriculum questions are playable directly from Play Levels.
        // This supplements (rather than replaces) the 12 original hands-on levels.
        line(content, "QUESTION CHALLENGE LEVELS", 19f, cyan, bold = true, top = 24)
        line(content, "50 questions per subject • choose a full topic or a 10-question level.",
            13f, muted, top = 5)
        val allCard = makeCard()
        line(allCard, "🏆  MASTER CHALLENGE", 17f, amber, bold = true)
        line(allCard, "All 350 questions • 7 subjects", 14f, muted, top = 5)
        progressLine(allCard, store.quizAnsweredCount(), QuizBank.questions.size, cyan)
        action(allCard, "Play all 350 questions  →", "primary") { startQuiz("ALL") }
        GameContent.lessons.forEachIndexed { index, lesson ->
            val answered = store.quizAnsweredCount(lesson.id)
            val accent = topicColor(index)
            val card = colorCard(Color.parseColor("#081D3C"), accent, 7)
            line(card, "${topicSymbol(index)}  ${lesson.name}", 17f, light, bold = true)
            line(card, "$answered / 50 answered  •  5 levels of 10 questions", 13f, muted, top = 5)
            progressLine(card, answered, 50, accent)
            action(card, "Play all 50 questions", "primary") { startQuiz(lesson.id) }
            action(card, "Choose a 10-question level", "muted") {
                val packs = arrayOf(
                    "Level 1 • Questions 1–10", "Level 2 • Questions 11–20",
                    "Level 3 • Questions 21–30", "Level 4 • Questions 31–40",
                    "Level 5 • Questions 41–50"
                )
                AlertDialog.Builder(this).setTitle(lesson.name)
                    .setItems(packs) { _, which -> startQuiz(lesson.id, which) }
                    .show()
            }
        }
    }

    private fun soundEffect(success: Boolean) {
        if (!store.soundEnabled) return
        try {
            val sound = ToneGenerator(AudioManager.STREAM_MUSIC, 30)
            sound.startTone(if (success) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK, 130)
            content.postDelayed({ sound.release() }, 180)
        } catch (_: RuntimeException) { /* Some emulators have no audio device. */ }
    }

    private fun renderPlay() {
        when {
            mode == PlayMode.CAMPAIGN && store.campaignIndex >= GameContent.puzzles.size &&
                (roundIndices.firstOrNull() ?: GameContent.puzzles.size) >= GameContent.puzzles.size -> {
                showCampaignEnd()
                return
            }
            mode == PlayMode.CLASSROOM && challengeFinished -> {
                showChallengeEnd()
                return
            }
        }
        val puzzle = currentPuzzle() ?: run {
            startCampaign()
            return
        }
        val number = if (mode == PlayMode.CAMPAIGN) roundIndices[roundPosition] + 1 else roundPosition + 1
        val total = if (mode == PlayMode.CAMPAIGN) GameContent.puzzles.size else roundIndices.size
        val modeName = when (mode) {
            PlayMode.CAMPAIGN -> "STORY LEVEL"
            PlayMode.PRACTICE -> "TOPIC PRACTICE"
            PlayMode.CLASSROOM -> "CLASSROOM CHALLENGE"
            PlayMode.REPLAY -> "LEVEL REPLAY"
        }
        val maxStages = if (puzzle.subTargets.isNotEmpty()) puzzle.subTargets.size
                        else if (puzzle.subPrompts.isNotEmpty()) puzzle.subPrompts.size
                        else 1
        line(content, "$modeName   •   Level $number / $total   •   Stage ${subRoundPosition + 1} of $maxStages", 12f, cyan, bold = true)
        line(content, "Campaign points: ${store.campaignPoints}", 14f, muted)
        if (mode == PlayMode.CLASSROOM) line(content, "Code: $challengeCode  •  Challenge points: $challengeScore", 13f, amber)
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = total * maxStages
            progress = (number - 1) * maxStages + subRoundPosition + if (solved) 1 else 0
            progressTintList = ColorStateList.valueOf(cyan)
            progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#334155"))
        }
        content.addView(progress, margins(top = 8, bottom = 14))
        val card = makeCard()
        line(card, GameContent.topicName(puzzle.topicId).uppercase(), 12f, cyan, bold = true)
        line(card, puzzle.title, 24f, light, bold = true, top = 8)
        val currentTarget = if (puzzle.subTargets.isNotEmpty()) puzzle.subTargets.getOrElse(subRoundPosition) { puzzle.target } else puzzle.target
        val instructionText = if (puzzle.type == PuzzleType.BINARY) "Make decimal $currentTarget using the four bits."
                              else if (puzzle.subPrompts.isNotEmpty()) puzzle.subPrompts.getOrElse(subRoundPosition) { puzzle.instructions }
                              else puzzle.instructions
        line(card, instructionText, 16f, muted, top = 12)
        val inputs = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(18), 0, 0)
        }
        card.addView(inputs)
        when (puzzle.type) {
            PuzzleType.BINARY -> renderBinary(inputs)
            PuzzleType.GATE -> renderGate(inputs, puzzle)
            PuzzleType.CHOICE -> renderChoices(inputs, puzzle)
            PuzzleType.ORDER -> renderOrder(inputs, puzzle)
            PuzzleType.MATCH -> renderMatches(inputs, puzzle)
        }
        feedbackLabel = line(content, feedback, 15f, feedbackColor, top = 12).apply {
            minHeight = dp(44)
        }
        if (solved) {
            action(content, if (roundPosition + 1 == roundIndices.size) "Finish" else "Next Puzzle", "positive") {
                advanceRound()
            }
        } else {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val hint = makeButton("Hint", "muted") {
                if (!hintUsed) {
                    hintUsed = true
                    store.recordHint(puzzle.topicId)
                }
                showFeedback("HINT: ${puzzle.hint}", amber)
            }
            val check = makeButton("Check Answer", "primary") { checkAnswer(puzzle) }
            row.addView(hint, LinearLayout.LayoutParams(0, dp(54), 1f))
            val params = LinearLayout.LayoutParams(0, dp(54), 2f).apply { marginStart = dp(8) }
            row.addView(check, params)
            content.addView(row, margins(top = 6))
        }
        if (mode != PlayMode.CAMPAIGN) {
            action(content, if (mode == PlayMode.CLASSROOM) "Leave challenge" else "Return to levels", "muted") {
                AlertDialog.Builder(this).setTitle("Leave this session?")
                    .setMessage("Unfinished answers in this session will not be saved.")
                    .setNegativeButton("Continue", null)
                    .setPositiveButton("Leave") { _, _ -> selectTab(Tab.LEVELS) }.show()
            }
        }
        if (mode == PlayMode.CAMPAIGN) {
            action(content, "Review this topic", "muted") {
                activeLesson = puzzle.topicId
                store.markLesson(puzzle.topicId)
                selectTab(Tab.LEARN)
            }
        }
    }

    /** Phase 3: select two tiles to swap; long-press a tile to drag it to another tile. */
    private fun renderOrder(host: LinearLayout, puzzle: Puzzle) {
        if (!GameContent.validPermutation(order, puzzle.pieces.size)) {
            order = puzzle.pieces.indices.toMutableList()
        }
        line(host, "Tap two tiles to swap, or long-press and drag a tile onto another.", 14f, cyan, top = 4)
        order.forEachIndexed { position, tileId ->
            val button = makeButton("${position + 1}   ${puzzle.pieces[tileId]}",
                if (position == selectedOrderPosition) "primary" else "muted") {
                if (!solved) {
                    if (selectedOrderPosition == -1) selectedOrderPosition = position
                    else {
                        if (position != selectedOrderPosition) swapTiles(selectedOrderPosition, position)
                        selectedOrderPosition = -1
                    }
                    redrawPuzzleInputs()
                }
            }
            button.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            button.setPadding(dp(14), dp(8), dp(10), dp(8))
            button.isEnabled = !solved
            button.setOnLongClickListener {
                if (solved) false else {
                    it.startDragAndDrop(ClipData.newPlainText("tile", "$position"),
                        View.DragShadowBuilder(it), position, 0)
                    true
                }
            }
            button.setOnDragListener { _, event ->
                if (event.action == DragEvent.ACTION_DROP && !solved) {
                    val from = event.localState as? Int ?: return@setOnDragListener false
                    if (from in order.indices) {
                        swapTiles(from, position)
                        selectedOrderPosition = -1
                        redrawPuzzleInputs()
                        true
                    } else false
                } else event.action == DragEvent.ACTION_DRAG_STARTED ||
                    event.action == DragEvent.ACTION_DRAG_ENTERED ||
                    event.action == DragEvent.ACTION_DRAG_LOCATION ||
                    event.action == DragEvent.ACTION_DRAG_EXITED ||
                    event.action == DragEvent.ACTION_DRAG_ENDED
            }
            host.addView(button, margins(top = 6))
        }
    }

    private fun redrawPuzzleInputs() {
        val previousScroll = scroll.scrollY
        selectTab(Tab.PLAY)
        scroll.post { scroll.scrollTo(0, previousScroll) }
    }

    private fun swapTiles(first: Int, second: Int) {
        val hold = order[first]
        order[first] = order[second]
        order[second] = hold
    }

    /** Phase 3: virtual hardware matching with touch and accessible drag fallback. */
    private fun renderMatches(host: LinearLayout, puzzle: Puzzle) {
        if (matches.size != puzzle.pieces.size) matches = MutableList(puzzle.pieces.size) { -1 }
        line(host, "Step 1: choose a component. Step 2: choose its function.", 14f, cyan, top = 3)
        puzzle.pieces.forEachIndexed { component, name ->
            val chosen = selectedComponent == component
            val slot = matches[component]
            val button = makeButton("${if (slot >= 0) "✓" else "○"}  $name${if (slot >= 0) " — assigned" else ""}",
                if (chosen) "primary" else "muted") {
                selectedComponent = component
                if (!solved) redrawPuzzleInputs()
            }
            button.isEnabled = !solved
            button.setOnLongClickListener {
                if (solved) false else {
                    it.startDragAndDrop(ClipData.newPlainText("component", "$component"),
                        View.DragShadowBuilder(it), component, 0)
                    true
                }
            }
            host.addView(button, margins(top = 5))
        }
        line(host, "FUNCTION SLOTS", 13f, amber, bold = true, top = 18)
        puzzle.descriptions.forEachIndexed { slot, description ->
            val assigned = matches.indexOf(slot)
            val text = "$description\n${if (assigned < 0) "Tap to assign a component" else "Assigned: ${puzzle.pieces[assigned]}"}"
            val button = makeButton(text, if (assigned >= 0) "primary" else "muted") {
                if (!solved) {
                    if (selectedComponent < 0) toast("Choose a component first")
                    else putComponent(selectedComponent, slot)
                }
            }
            button.isEnabled = !solved
            button.setPadding(dp(12), dp(7), dp(12), dp(7))
            button.setOnDragListener { _, event ->
                if (event.action == DragEvent.ACTION_DROP && !solved) {
                    val component = event.localState as? Int ?: return@setOnDragListener false
                    if (component in puzzle.pieces.indices) { putComponent(component, slot); true } else false
                } else event.action == DragEvent.ACTION_DRAG_STARTED ||
                    event.action == DragEvent.ACTION_DRAG_ENTERED ||
                    event.action == DragEvent.ACTION_DRAG_LOCATION ||
                    event.action == DragEvent.ACTION_DRAG_EXITED ||
                    event.action == DragEvent.ACTION_DRAG_ENDED
            }
            host.addView(button, margins(top = 7))
        }
    }

    private fun putComponent(component: Int, slot: Int) {
        for (i in matches.indices) if (matches[i] == slot) matches[i] = -1
        matches[component] = slot
        selectedComponent = -1
        redrawPuzzleInputs()
    }

    private fun renderBinary(host: LinearLayout) {
        line(host, "Tap each bit to change 0 ↔ 1", 14f, muted)
        val weights = intArrayOf(8, 4, 2, 1)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        weights.forEachIndexed { i, weight ->
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(dp(2), 0, dp(2), 0)
            }
            line(column, "$weight", 13f, muted)
            val switch = makeButton(bits[i].toString(), if (bits[i] == 1) "primary" else "muted") {
                bits[i] = 1 - bits[i]
                renderValue("Decimal value: ${GameContent.binaryValue(bits)}")
                // Preserve other inputs and redraw only this button.
                val button = it
                button.text = bits[i].toString()
                button.backgroundTintList = ColorStateList.valueOf(if (bits[i] == 1) Color.parseColor("#086BBE") else Color.parseColor("#334155"))
            }
            switch.textSize = 21f
            switch.isEnabled = !solved
            column.addView(switch, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(64)))
            row.addView(column, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        host.addView(row, margins(top = 8))
        valueLabel = line(host, "Decimal value: ${GameContent.binaryValue(bits)}", 16f, cyan, bold = true, top = 12)
    }

    private fun renderGate(host: LinearLayout, puzzle: Puzzle) {
        line(host, "${puzzle.gate} gate   •   Target output: ${puzzle.target}", 15f, muted)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val a = makeButton("A = $inputA", if (inputA == 1) "primary" else "muted") {
            inputA = 1 - inputA
            it.text = "A = $inputA"
            it.backgroundTintList = ColorStateList.valueOf(if (inputA == 1) Color.parseColor("#086BBE") else Color.parseColor("#334155"))
            renderValue("Output: ${GameContent.gateValue(puzzle.gate, inputA, inputB)}")
        }
        val b = makeButton("B = $inputB", if (inputB == 1) "primary" else "muted") {
            inputB = 1 - inputB
            it.text = "B = $inputB"
            it.backgroundTintList = ColorStateList.valueOf(if (inputB == 1) Color.parseColor("#086BBE") else Color.parseColor("#334155"))
            renderValue("Output: ${GameContent.gateValue(puzzle.gate, inputA, inputB)}")
        }
        a.isEnabled = !solved
        b.isEnabled = !solved
        row.addView(a, LinearLayout.LayoutParams(0, dp(58), 1f))
        row.addView(b, LinearLayout.LayoutParams(0, dp(58), 1f).apply { marginStart = dp(8) })
        host.addView(row, margins(top = 10))
        valueLabel = line(host, "Output: ${GameContent.gateValue(puzzle.gate, inputA, inputB)}", 16f, cyan, bold = true, top = 12)
    }

    private fun renderChoices(host: LinearLayout, puzzle: Puzzle) {
        val currentOptions = if (puzzle.subOptionsList.isNotEmpty()) puzzle.subOptionsList.getOrElse(subRoundPosition) { puzzle.options } else puzzle.options
        val group = RadioGroup(this).apply { orientation = LinearLayout.VERTICAL }
        currentOptions.forEachIndexed { index, option ->
            val item = RadioButton(this).apply {
                id = View.generateViewId()
                text = option
                textSize = 17f
                setTextColor(light)
                buttonTintList = ColorStateList.valueOf(cyan)
                setPadding(dp(8), dp(10), dp(8), dp(10))
                isChecked = selectedAnswer == index
                isEnabled = !solved
                setOnClickListener { selectedAnswer = index }
            }
            group.addView(item)
        }
        host.addView(group)
    }

    private fun renderValue(value: String) { valueLabel?.text = value }

    private fun showFeedback(value: String, color: Int) {
        feedback = value
        feedbackColor = color
        feedbackLabel?.text = value
        feedbackLabel?.setTextColor(color)
    }

    private fun checkAnswer(puzzle: Puzzle) {
        if (solved) return
        if (puzzle.type == PuzzleType.CHOICE && selectedAnswer < 0) {
            showFeedback("Choose an answer before checking.", amber)
            return
        }
        val maxStages = if (puzzle.subTargets.isNotEmpty()) puzzle.subTargets.size
                        else if (puzzle.subPrompts.isNotEmpty()) puzzle.subPrompts.size
                        else 1
        val correct = GameContent.check(puzzle, bits, inputA, inputB, selectedAnswer, order, matches, subRoundPosition)
        val pristine = wrongAttempts == 0 && !hintUsed
        val previousBadges = store.unlockedBadges()
        store.recordAttempt(puzzle.topicId, correct, pristine)
        if (!correct) {
            soundEffect(false)
            wrongAttempts++
            showFeedback("Not quite. Try again, or use Hint for guidance.", red)
            return
        }
        if (subRoundPosition < maxStages - 1) {
            soundEffect(true)
            subRoundPosition++
            resetPuzzleInputs()
            showFeedback("Correct! Stage $subRoundPosition / $maxStages complete. Next challenge...", green)
            selectTab(Tab.PLAY)
            return
        }
        solved = true
        subRoundPosition = 0
        soundEffect(true)
        when (mode) {
            PlayMode.CAMPAIGN -> store.finishCampaignLevel(roundIndices[roundPosition])
            PlayMode.CLASSROOM -> challengeScore += if (pristine) 10 else 5
            PlayMode.PRACTICE, PlayMode.REPLAY -> Unit
        }
        val pointsText = when (mode) {
            PlayMode.CAMPAIGN -> "+10 campaign points"
            PlayMode.CLASSROOM -> if (pristine) "+10 challenge points" else "+5 challenge points"
            PlayMode.PRACTICE -> "Practice completed"
            PlayMode.REPLAY -> "Level replay completed"
        }
        showFeedback("Correct! $pointsText\n${puzzle.explanation}", green)
        val newBadges = store.unlockedBadges() - previousBadges
        if (newBadges.isNotEmpty()) toast("Badge unlocked: ${badgeInfo.firstOrNull { it.id in newBadges }?.title ?: "New badge"}")
        selectTab(Tab.PLAY)
    }

    private fun advanceRound() {
        if (!solved) return
        if (mode == PlayMode.CAMPAIGN) {
            startCampaign()
            return
        }
        roundPosition++
        if (roundPosition >= roundIndices.size) {
            if (mode == PlayMode.REPLAY) {
                selectTab(Tab.LEVELS)
                toast("Replay completed!")
            } else if (mode == PlayMode.CLASSROOM) {
                challengeFinished = true
                store.recordClassroomResult(challengeCode, challengeScore, roundIndices.size * 10)
                selectTab(Tab.PLAY)
            } else {
                activeLesson = GameContent.puzzles[roundIndices.first()].topicId
                selectTab(Tab.LEARN)
                toast("All practice puzzles complete!")
            }
            return
        }
        resetPuzzleInputs()
        selectTab(Tab.PLAY)
    }

    private fun showCampaignEnd() {
        line(content, "CAMPAIGN COMPLETE", 13f, cyan, bold = true)
        val card = makeCard()
        line(card, "All ${GameContent.puzzles.size} puzzles cleared!", 25f, light, bold = true)
        line(card, "Campaign points: ${store.campaignPoints}\nGreat job applying computer studies concepts.", 16f, muted, top = 12)
        action(content, "View learning report", "primary") { selectTab(Tab.PROGRESS) }
        action(content, "Practice a topic", "muted") { activeLesson = null; selectTab(Tab.LEARN) }
        action(content, "Restart all progress", "danger") { confirmReset() }
    }

    private fun showChallengeEnd() {
        val possible = roundIndices.size * 10
        line(content, "CLASSROOM RESULTS", 13f, cyan, bold = true)
        val card = makeCard()
        line(card, "Challenge complete!", 24f, light, bold = true)
        line(card, "$challengeCode", 16f, cyan, top = 10)
        line(card, "Your score: $challengeScore / $possible", 22f, green, bold = true, top = 10)
        line(card, "10 points for a first-try answer without a hint; otherwise 5 points when solved.", 14f, muted, top = 10)
        action(content, "Share results", "primary") {
            shareText("ByteQuest classroom results\nCode: $challengeCode\nScore: $challengeScore / $possible\nCompleted offline on my device.")
        }
        action(content, "Join another challenge", "muted") { selectTab(Tab.CLASS) }
        action(content, "Return to campaign", "muted") { startCampaign() }
    }

    // ----- Interactive lesson cards -----
    private fun renderLearn() {
        val selected=GameContent.lessons.firstOrNull{it.id==activeLesson}
        if(selected!=null){
            val lessonIdx=GameContent.lessons.indexOf(selected)
            pageTitle(selected.name,"Lesson ${lessonIdx+1} of ${GameContent.lessons.size}",cyan,Tab.LEARN)
            val art=colorCard(Color.parseColor("#092455"),Color.parseColor("#126DE1"),3)
            val visual=when(lessonIdx){
                0->"1010₂  =  10₁₀"; 1->"A ∧ B  →  1"; 2->"{ code }";
                3->"BUG  →  FIX"; 4->"PC  ↔  ROUTER"; 5->"START  →  END";else->"CPU  •  RAM  •  SSD"
            }
            line(art,visual,29f,cyan,bold=true,top=10).gravity=Gravity.CENTER
            line(art,selected.subtitle,13f,light,top=12).gravity=Gravity.CENTER
            val lesson=colorCard(Color.parseColor("#081B36"),Color.parseColor("#1957A1"))
            line(lesson,"WHAT WILL YOU LEARN?",13f,cyan,bold=true)
            line(lesson,selected.summary,16f,light,top=10)
            val points=colorCard(Color.parseColor("#081B36"),Color.parseColor("#1957A1"))
            line(points,"KEY POINTS",16f,cyan,bold=true)
            selected.keyIdeas.forEach{line(points,"✓  $it",14f,light,top=10)}
            val example=colorCard(Color.parseColor("#0B1D3A"),Color.parseColor("#1957A1"))
            line(example,"WORKED EXAMPLE",13f,cyan,bold=true)
            line(example,selected.example,15f,light,top=10)
            line(example,"STUDY TIP",13f,cyan,bold=true,top=17)
            line(example,selected.studyTip,14f,muted,top=8)
            action(content,"Practice ${selected.name} puzzles","primary"){startPractice(selected.id)}
            action(content,"Answer 50 lesson questions","primary"){startQuiz(selected.id)}
            QuizBank.studySources[selected.id]?.let { source ->
                action(content,"External lesson reference: ${source.title}","muted"){openStudySource(selected.id)}
            }
            return
        }
        pageTitle("LESSONS", "Learn step by step • 7 topics",cyan,null)
        GameContent.lessons.forEachIndexed{i,lesson->
            menuRow(lesson.name,"${if(store.learned(lesson.id))"✓ Reviewed" else "Not yet reviewed"} • 50 questions",topicSymbol(i),topicColor(i)){
                activeLesson=lesson.id
                val previously=store.unlockedBadges()
                store.markLesson(lesson.id)
                if((store.unlockedBadges()-previously).isNotEmpty())toast("New achievement unlocked!")
                selectTab(Tab.LEARN)
            }
        }
    }

    // ----- 350 question knowledge bank: short topic packs, full challenges and explanations -----
    private fun startQuiz(lessonId: String, packIndex: Int? = null) {
        quizIds = (if (packIndex == null) QuizBank.forLesson(lessonId)
            else QuizBank.tenQuestionPack(lessonId, packIndex)).map { it.id }
        quizClassCode = ""
        quizPosition = 0
        quizSelected = -1
        quizSubmitted = false
        quizSessionCorrect = 0
        quizCompleted = false
        selectTab(Tab.QUIZ)
    }

    private fun startClassroomQuiz(challenge: QuizChallenge) {
        quizIds = QuizBank.challengeQuestionIds(challenge)
        quizClassCode = challenge.code
        quizPosition = 0
        quizSelected = -1
        quizSubmitted = false
        quizSessionCorrect = 0
        quizCompleted = false
        selectTab(Tab.QUIZ)
    }

    private fun openStudySource(lessonId: String) {
        val source = QuizBank.studySources[lessonId] ?: return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.url)))
        } catch (_: ActivityNotFoundException) {
            toast("No browser found. You can still study the lesson offline.")
        }
    }

    private fun renderQuiz() {
        if (quizIds.isNotEmpty() && quizCompleted) {
            line(content, "QUIZ RESULTS", 13f, cyan, bold = true)
            val total = quizIds.size
            val results = makeCard()
            line(results, "${quizSessionCorrect} / $total correct", 28f, light, bold = true)
            line(results, "${if (total == 0) 0 else quizSessionCorrect * 100 / total}% accuracy this session", 17f, green, top = 7)
            line(results, "${store.quizAnsweredCount()} / ${QuizBank.questions.size} unique questions attempted overall", 14f, muted, top = 12)
            if (quizClassCode.isNotEmpty()) {
                line(results, "Classroom challenge: $quizClassCode", 15f, cyan, top = 10)
                action(results, "Share classroom quiz result", "primary") {
                    shareText("ByteQuest classroom quiz result\nCode: $quizClassCode\nScore: ${quizSessionCorrect * 10} / ${total * 10}\nCompleted offline on my device.")
                }
            }
            val retryIds = quizIds.filter { !store.quizMastered(it) }
            if (retryIds.isNotEmpty()) {
                action(results, "Retry ${retryIds.size} not-yet-mastered questions", "primary") {
                    quizIds = retryIds
                    quizClassCode = ""
                    quizPosition = 0
                    quizSelected = -1
                    quizSubmitted = false
                    quizSessionCorrect = 0
                    quizCompleted = false
                    selectTab(Tab.QUIZ)
                }
            }
            action(content, "Choose another quiz", "primary") {
                quizIds = emptyList(); quizClassCode = ""; quizCompleted = false; selectTab(Tab.QUIZ)
            }
            action(content, "View learning report", "muted") { selectTab(Tab.PROGRESS) }
            return
        }
        if (quizIds.isEmpty()) {
            pageTitle("QUIZ CHALLENGE", "Choose a topic to start",Color.parseColor("#E09EFF"),null)
            val overview=colorCard(Color.parseColor("#0C1E43"),Color.parseColor("#2A67B7"),0)
            line(overview,"🏆  350 QUESTIONS AVAILABLE",17f,amber,bold=true)
            line(overview,"${store.quizAnsweredCount()} attempted  •  ${store.quizMasteredCount()} mastered",13f,muted,top=7)
            progressLine(overview,store.quizAnsweredCount(),QuizBank.questions.size,cyan)
            action(overview,"Play full 350-question challenge  →","primary"){startQuiz("ALL")}
            GameContent.lessons.forEachIndexed{i,lesson->
                val done=store.quizAnsweredCount(lesson.id)
                val card=colorCard(Color.parseColor("#081D3C"),Color.parseColor("#1359A6"),7)
                val row=LinearLayout(this).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
                val chip=TextView(this).apply {
                    text=topicSymbol(i);textSize=17f;gravity=Gravity.CENTER;setTextColor(light)
                    typeface=Typeface.DEFAULT_BOLD;background=pillShape(Color.parseColor("#11335D"),topicColor(i),9)
                }
                row.addView(chip,LinearLayout.LayoutParams(dp(45),dp(45)))
                val labels=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(dp(12),0,0,0)}
                line(labels,lesson.name,16f,light,bold=true)
                line(labels,"50 Questions   •   $done/50",12f,muted,top=3)
                row.addView(labels,LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f))
                line(row,"›",27f,topicColor(i),bold=true)
                card.addView(row)
                card.isClickable=true;card.isFocusable=true
                card.setOnClickListener{startQuiz(lesson.id)}
                action(card,"Quick practice: choose a 10-question set","muted"){
                    val opts=arrayOf("Set 1: Questions 1–10","Set 2: Questions 11–20","Set 3: Questions 21–30","Set 4: Questions 31–40","Set 5: Questions 41–50")
                    AlertDialog.Builder(this).setTitle(lesson.name).setItems(opts){_,which->startQuiz(lesson.id,which)}.show()
                }
            }
            return
        }
        val question = quizIds.getOrNull(quizPosition)?.let { QuizBank.findById(it) }
        if (question == null) {
            quizIds = emptyList()
            selectTab(Tab.QUIZ)
            return
        }
        pageTitle(GameContent.topicName(question.lessonId),"Question ${quizPosition + 1} / ${quizIds.size}",light,Tab.QUIZ)
        if (quizClassCode.isNotEmpty()) line(content, "Classroom: $quizClassCode", 12f, amber, top = 6)
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = quizIds.size
            progress = quizPosition + if (quizSubmitted) 1 else 0
            progressTintList = ColorStateList.valueOf(cyan)
            progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#334155"))
        }
        content.addView(progress, margins(top = 10, bottom = 13))
        val questionCard = makeCard()
        line(questionCard, question.prompt, 21f, light, bold = true)
        question.options.forEachIndexed { index, choice ->
            val correct=quizSubmitted && index==question.correctIndex
            val chosen=quizSelected==index
            val accent=if(correct) green else if(chosen) cyan else Color.parseColor("#1A518C")
            val fill=if(correct) Color.parseColor("#063D35") else if(chosen) Color.parseColor("#153C6A") else Color.parseColor("#0A1E3B")
            val option=LinearLayout(this).apply {
                orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
                setPadding(dp(10),dp(11),dp(10),dp(11))
                background=pillShape(fill,accent,12)
                isClickable=!quizSubmitted;isFocusable=!quizSubmitted
                setOnClickListener { if(!quizSubmitted){quizSelected=index;selectTab(Tab.QUIZ)} }
            }
            val badge=TextView(this).apply{
                text="${'A'+index}";textSize=15f;gravity=Gravity.CENTER
                setTextColor(if(chosen||correct)bg else light)
                typeface=Typeface.DEFAULT_BOLD
                background=pillShape(if(chosen||correct)accent else Color.parseColor("#27528B"),accent,100)
            }
            option.addView(badge,LinearLayout.LayoutParams(dp(31),dp(31)))
            val opt=TextView(this).apply {text=choice;textSize=15f;setTextColor(light);setPadding(dp(12),0,dp(4),0)}
            option.addView(opt,LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f))
            questionCard.addView(option,margins(top=7))
        }
        if (!quizSubmitted) {
            line(questionCard, "Reference lesson: ${GameContent.topicName(question.lessonId)}", 13f, cyan, top = 13)
            action(content, "Submit answer", "primary") {
                if (quizSelected !in question.options.indices) {
                    toast("Select an answer before submitting.")
                } else {
                    val correct = quizSelected == question.correctIndex
                    val previous = store.unlockedBadges()
                    quizSubmitted = true
                    quizSessionCorrect += if (correct) 1 else 0
                    store.recordQuizAnswer(question, correct)
                    soundEffect(correct)
                    if ((store.unlockedBadges() - previous).isNotEmpty()) toast("New achievement unlocked!")
                    selectTab(Tab.QUIZ)
                }
            }
            action(content, "Review lesson (resume quiz later)", "muted") {
                activeLesson = question.lessonId
                store.markLesson(question.lessonId)
                selectTab(Tab.LEARN)
            }
        } else {
            val correct = quizSelected == question.correctIndex
            val review = makeCard()
            line(review, if (correct) "✓ Correct answer!" else "Review this answer", 20f, if (correct) green else amber, bold = true)
            line(review, "Correct: ${QuizBank.correctOption(question)}", 17f, light, bold = true, top = 9)
            line(review, question.explanation, 15f, muted, top = 9)
            line(review, "LESSON REFERENCE", 12f, cyan, bold = true, top = 18)
            line(review, QuizBank.lessonReference(question), 14f, light, top = 7)
            action(review, "Open full lesson", "muted") {
                activeLesson = question.lessonId
                store.markLesson(question.lessonId)
                selectTab(Tab.LEARN)
            }
            QuizBank.studySources[question.lessonId]?.let { source ->
                line(review, "Source: ${source.title}", 13f, muted, top = 12)
                action(review, "Open source (online)", "muted") { openStudySource(question.lessonId) }
            }
            action(content, if (quizPosition == quizIds.lastIndex) "Finish quiz  →" else "Next question  →", "primary") {
                quizPosition++
                quizSelected = -1
                quizSubmitted = false
                if (quizPosition >= quizIds.size) {
                    quizCompleted = true
                    if (quizClassCode.isNotEmpty()) store.recordClassroomResult(quizClassCode, quizSessionCorrect * 10, quizIds.size * 10)
                }
                selectTab(Tab.QUIZ)
            }
        }
        action(content, "Exit to question bank", "muted") {
            AlertDialog.Builder(this).setTitle("Exit this quiz?")
                .setMessage("Submitted answers are saved, but this quiz session will end.")
                .setNegativeButton("Continue quiz", null)
                .setPositiveButton("Exit") { _, _ ->
                    quizIds = emptyList()
                    quizClassCode = ""
                    quizCompleted = false
                    selectTab(Tab.QUIZ)
                }.show()
        }
    }

    private data class Badge(val id: String, val title: String, val details: String)
    private val badgeInfo = listOf(
        Badge("FIRST", "First Steps", "Solve your first puzzle."),
        Badge("READER", "Curious Reader", "Review three lessons."),
        Badge("BINARY", "Binary Builder", "Solve two number-system puzzles."),
        Badge("LOGIC", "Logic Explorer", "Solve three logic-gate puzzles."),
        Badge("FLAWLESS", "Sharp Thinker", "Solve three puzzles with no hint or incorrect attempt."),
        Badge("ALLROUNDER", "All-Rounder", "Solve a puzzle in all seven study topics."),
        Badge("GRADUATE", "ByteQuest Graduate", "Complete all ${GameContent.puzzles.size} campaign puzzles."),
        Badge("CLASSROOM", "Classroom Challenger", "Complete one classroom challenge."),
        Badge("QUIZ_FIRST", "Question Explorer", "Answer your first lesson question."),
        Badge("QUIZ_HALF", "Knowledge Seeker", "Attempt 25 different lesson questions."),
        Badge("QUIZ_50", "First Fifty", "Attempt at least 50 different lesson questions."),
        Badge("QUIZ_TOPIC", "Subject Specialist", "Attempt all 50 questions in one topic."),
        Badge("QUIZ_175", "Halfway Hero", "Attempt at least 175 different questions."),
        Badge("QUIZ_350", "Knowledge Explorer", "Attempt all 350 questions."),
        Badge("QUIZ_MASTER", "Master of All Topics", "Correctly answer all 350 questions at least once.")
    )

    private fun renderProgress() {
        pageTitle("LEARNING REPORTS", "Your academic journey at a glance",Color.parseColor("#DA99FF"),Tab.MORE)
        val attempted=store.quizAnsweredCount()
        val accuracy=if(store.quizTopicAttempts()==0) 0 else store.quizTopicCorrect()*100/store.quizTopicAttempts()
        val summary=colorCard(Color.parseColor("#0D244C"),Color.parseColor("#286BC5"),3)
        line(summary,"OVERALL LEARNING PROGRESS",13f,muted,bold=true)
        line(summary,"$attempted / ${QuizBank.questions.size}",32f,light,bold=true,top=9)
        line(summary,"${attempted*100/QuizBank.questions.size}% of question bank completed",14f,cyan,top=4)
        progressLine(summary,attempted,QuizBank.questions.size,cyan)
        line(summary,"${store.quizMasteredCount()} mastered   •   $accuracy% accuracy   •   ${store.campaignPoints} XP",13f,green,top=14)
        val topics=colorCard(Color.parseColor("#0B1E3D"),Color.parseColor("#2261B0"),14)
        line(topics,"TOPIC PERFORMANCE",18f,light,bold=true)
        GameContent.lessons.forEachIndexed { i,lesson ->
            val done=store.quizAnsweredCount(lesson.id)
            val total=QuizBank.forLesson(lesson.id).size
            val attempts=store.quizTopicAttempts(lesson.id)
            val pct=if(attempts==0)0 else store.quizTopicCorrect(lesson.id)*100/attempts
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
            line(row,lesson.name,13f,muted).apply{layoutParams=LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f)}
            line(row,"${pct}%",12f,topicColor(i),bold=true)
            topics.addView(row,margins(top=12))
            progressLine(topics,done,total,topicColor(i))
            line(topics,"$done / $total attempted   •   ${store.quizMasteredCount(lesson.id)} mastered",11f,muted,top=3)
        }
        val activity=colorCard(Color.parseColor("#0C203D"),Color.parseColor("#2461A8"),13)
        line(activity,"RECENT ACTIVITY",17f,light,bold=true)
        line(activity,"✓  ${store.campaignIndex} / ${GameContent.puzzles.size} adventure levels completed",14f,green,top=10)
        line(activity,"✓  ${GameContent.lessons.count{store.learned(it.id)}} / 7 lessons reviewed",14f,green,top=8)
        line(activity,"★  ${store.unlockedBadges().size} / ${badgeInfo.size} achievements unlocked",14f,amber,top=8)
        action(activity,"View achievements  →","muted"){selectTab(Tab.BADGES)}
        action(content,"Continue studying  →","primary"){selectTab(Tab.LEARN)}
    }

    private fun renderBadges() {
        pageTitle("ACHIEVEMENTS", "Complete challenges and earn badges",Color.parseColor("#DB9BFF"),Tab.MORE)
        val unlocked=store.unlockedBadges()
        val head=colorCard(Color.parseColor("#102049"),Color.parseColor("#286EC5"),4)
        line(head,"★  ${unlocked.size} / ${badgeInfo.size} Unlocked",22f,amber,bold=true)
        progressLine(head,unlocked.size,badgeInfo.size,green)
        badgeInfo.chunked(3).forEachIndexed { rowIndex, badges ->
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            badges.forEachIndexed { col,badge ->
                val earned=badge.id in unlocked
                val accent=listOf(amber,Color.parseColor("#A883FF"),cyan,red,green)[(rowIndex*3+col)%5]
                val cell=LinearLayout(this).apply {
                    orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER
                    setPadding(dp(4),dp(15),dp(4),dp(12))
                    background=pillShape(Color.parseColor("#0D2041"),if(earned)accent else Color.parseColor("#234263"),13)
                }
                val symbol=line(cell,if(earned)"🏅" else "◇",30f,if(earned)accent else muted,bold=true)
                symbol.gravity=Gravity.CENTER
                val name=line(cell,badge.title,12f,light,bold=true,top=8)
                name.gravity=Gravity.CENTER
                name.maxLines=2
                val hint=line(cell,if(earned)"Unlocked" else "Locked",11f,if(earned)green else muted,top=8)
                hint.gravity=Gravity.CENTER
                cell.isClickable=true;cell.isFocusable=true
                cell.setOnClickListener{
                    AlertDialog.Builder(this).setTitle(badge.title)
                        .setMessage("${badge.details}\n\n${if(earned)"Unlocked!" else "Keep learning to unlock this achievement."}")
                        .setPositiveButton("OK",null).show()
                }
                row.addView(cell,LinearLayout.LayoutParams(0,dp(143),1f).apply{if(col>0)marginStart=dp(7)})
            }
            content.addView(row,margins(top=8))
        }
    }

    // ----- Optional offline classroom challenge flow -----
    private fun renderClass() {
        pageTitle("CLASSROOM MODE","Learn together, challenge together",Color.parseColor("#DA9BFF"),Tab.MORE)
        val notice=colorCard(Color.parseColor("#0C2247"),Color.parseColor("#235CB1"),2)
        line(notice,"👥  CREATE & JOIN CHALLENGES",17f,cyan,bold=true)
        line(notice,"Share a classroom code for an offline quiz or puzzle challenge. No account or internet required.",14f,muted,top=7)
        val create = makeCard()
        line(create, "Create a classroom code", 20f, light, bold = true)
        line(create, "Pick a topic and number of puzzles. Share the generated code with students.", 14f, muted, top = 7)
        val topicSpinner = Spinner(this)
        val topicLabels = GameContent.allTopics.map { GameContent.topicName(it) }
        topicSpinner.adapter = spinnerAdapter(topicLabels)
        create.addView(topicSpinner, margins(top = 14))
        val countSpinner = Spinner(this)
        create.addView(countSpinner, margins(top = 8))
        fun updateCounts(position: Int) {
            val id = GameContent.allTopics[position]
            val total = GameContent.puzzleIndices(id).size
            countSpinner.adapter = spinnerAdapter((1..total).map { "$it puzzle${if (it == 1) "" else "s"}" })
            countSpinner.setSelection(total - 1)
        }
        topicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { updateCounts(position) }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        updateCounts(0)
        val generated = line(create, "", 19f, cyan, bold = true, top = 10)
        val copy = action(create, "Copy code", "muted") {
            val code = generated.text.toString()
            if (code.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("ByteQuest challenge code", code))
                toast("Challenge code copied")
            }
        }
        val share = action(create, "Share code", "muted") {
            val code = generated.text.toString()
            if (code.isNotEmpty()) shareText("Join my ByteQuest challenge!\nCode: $code\nOpen ByteQuest → Class → Join challenge.")
        }
        copy.visibility = View.GONE
        share.visibility = View.GONE
        action(create, "Generate code", "primary") {
            val topicId = GameContent.allTopics[topicSpinner.selectedItemPosition]
            val challenge = GameContent.makeChallenge(topicId, countSpinner.selectedItemPosition + 1)
            if (challenge != null) {
                generated.text = challenge.code
                copy.visibility = View.VISIBLE
                share.visibility = View.VISIBLE
            }
        }
        val join = makeCard()
        line(join, "Join a classroom challenge", 20f, light, bold = true)
        line(join, "Enter a puzzle code (BQ4-LOG-3) or quiz code (BQQ-NUM-50 / BQQ-ALL-350).", 14f, muted, top = 7)
        val codeField = EditText(this).apply {
            hint = "BQQ-NUM-50"
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            setTextColor(light)
            setHintTextColor(Color.parseColor("#94A3B8"))
            backgroundTintList = ColorStateList.valueOf(cyan)
        }
        join.addView(codeField, margins(top = 8))
        action(join, "Start challenge", "primary") {
            val parsedQuiz = QuizBank.parseQuizChallenge(codeField.text.toString())
            val parsedPuzzle = GameContent.parseChallenge(codeField.text.toString())
            when {
                parsedQuiz != null -> startClassroomQuiz(parsedQuiz)
                parsedPuzzle != null -> startClassroom(parsedPuzzle)
                else -> AlertDialog.Builder(this)
                    .setTitle("Invalid challenge code")
                    .setMessage("Use BQ4-TOPIC-COUNT for puzzle levels or BQQ-TOPIC-COUNT for lesson questions, such as BQQ-NUM-50.")
                    .setPositiveButton("OK", null).show()
            }
        }
        val quizChallenge = makeCard()
        line(quizChallenge, "Create a classroom question code", 20f, light, bold = true)
        line(quizChallenge, "BQQ codes select 1–50 questions per topic, or up to 350 for all topics. Old BQ4 puzzle codes still work.", 14f, muted, top = 7)
        val quizTopicSpinner = Spinner(this)
        quizTopicSpinner.adapter = spinnerAdapter(GameContent.allTopics.map { GameContent.topicName(it) })
        quizChallenge.addView(quizTopicSpinner, margins(top = 10))
        val quizCountSpinner = Spinner(this)
        quizChallenge.addView(quizCountSpinner, margins(top = 8))
        var quizCountOptions = listOf(5, 10, 20, 25, 50, 100, 175, 350)
        fun updateQuizCounts(position: Int) {
            val total = QuizBank.forLesson(GameContent.allTopics[position]).size
            quizCountOptions = listOf(5, 10, 20, 25, 50, 100, 175, 350).filter { it <= total }
            quizCountSpinner.adapter = spinnerAdapter(quizCountOptions.map { "$it questions" })
            quizCountSpinner.setSelection(quizCountOptions.lastIndex)
        }
        quizTopicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { updateQuizCounts(position) }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        updateQuizCounts(0)
        val newQuizCode = line(quizChallenge, "", 19f, cyan, bold = true, top = 10)
        val shareQuizCode = action(quizChallenge, "Share classroom quiz code", "muted") {
            if (newQuizCode.text.isNotEmpty()) shareText("Join my ByteQuest classroom quiz!\nCode: ${newQuizCode.text}\nOpen ByteQuest → Class → Join challenge.")
        }
        shareQuizCode.visibility = View.GONE
        action(quizChallenge, "Generate question code", "primary") {
            val id = GameContent.allTopics[quizTopicSpinner.selectedItemPosition]
            val created = QuizBank.makeQuizChallenge(id, quizCountOptions[quizCountSpinner.selectedItemPosition])
            if (created != null) {
                newQuizCode.text = created.code
                shareQuizCode.visibility = View.VISIBLE
            }
        }
        val status = makeCard()
        line(status, "Most recent result", 18f, light, bold = true)
        if (store.lastClassCode.isEmpty()) {
            line(status, "No classroom challenges completed yet.", 14f, muted, top = 8)
        } else {
            line(status, "${store.lastClassCode}: ${store.lastClassScore} / ${store.lastClassPossible}", 16f, green, top = 8)
            line(status, "Completed challenges: ${store.classroomCompleted}", 14f, muted, top = 6)
            action(status, "Share last result", "muted") {
                shareText("ByteQuest classroom results\nCode: ${store.lastClassCode}\nScore: ${store.lastClassScore} / ${store.lastClassPossible}")
            }
        }
        line(content, "Privacy note: codes are not passwords, and results are stored on each device. Shared screenshots or messages are not an authenticated teacher leaderboard.",
            13f, muted, top = 14)
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle("Reset ByteQuest progress?")
            .setMessage("This deletes campaign progress, 350-question results, lesson views, topic reports, badges, and classroom results on this device.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Reset everything") { _, _ ->
                store.resetAll()
                quizIds = emptyList()
                quizClassCode = ""
                quizCompleted = false
                startCampaign()
            }
            .show()
    }

    private fun shareText(message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(intent, "Share ByteQuest"))
    }

    private fun percentText(correct: Int, attempts: Int) =
        if (attempts == 0) "Not started" else "${correct * 100 / attempts}%"

    // ----- Minimal, consistent Android Views -----
    private fun makeCard(): LinearLayout = colorCard(
        Color.parseColor("#091B39"),Color.parseColor("#17569D"),12)

    private fun line(parent: LinearLayout, value: String, size: Float, color: Int,
                     bold: Boolean = false, top: Int = 0): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        setLineSpacing(dp(3).toFloat(), 1f)
        val lp = if (parent.orientation == LinearLayout.HORIZONTAL) {
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(top)
            }
        } else {
            margins(top = top)
        }
        parent.addView(this, lp)
    }

    private fun margins(top: Int = 0, bottom: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            .apply { topMargin = dp(top); bottomMargin = dp(bottom) }

    private fun makeButton(title: String, style: String, handler: (Button) -> Unit): Button = Button(this).apply {
        text = title
        isAllCaps = false
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(light)
        setPadding(dp(12),dp(10),dp(12),dp(10))
        minHeight=dp(49)
        background = when (style) {
            "primary" -> GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.parseColor("#01BFCF"),Color.parseColor("#2336E9"))).apply {
                cornerRadius=dp(14).toFloat();setStroke(dp(1),Color.parseColor("#32D8FF"))
            }
            "positive" -> pillShape(Color.parseColor("#046A53"),green,14)
            "danger" -> pillShape(Color.parseColor("#662039"),red,14)
            else -> pillShape(Color.parseColor("#10264B"),Color.parseColor("#205B9C"),14)
        }
        setOnClickListener { handler(this) }
    }

    private fun action(parent: LinearLayout, text: String, style: String, handler: () -> Unit): Button {
        val button = makeButton(text, style) { handler() }
        parent.addView(button, margins(top = 10))
        return button
    }

    private fun spinnerAdapter(labels: List<String>): ArrayAdapter<String> =
        object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(light)
                view.setPadding(dp(8), dp(10), dp(8), dp(10))
                return view
            }
        }.apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
