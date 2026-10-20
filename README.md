# ByteQuest — Neon Reference Edition
**Android puzzle adventure for Computer Studies students** · **Made by MR.DEV**

This source project implements the user-selected first neon ByteQuest concept as native Android layouts and Kotlin views. The visual style closely follows the supplied reference (dark navy backgrounds, glowing cyan and colored accents, rounded cards, five-tab bottom navigation). Responsive Android text rendering, device sizes and font settings can differ from a fixed image, so this is not a pixel-for-pixel reproduction of the poster.

## What's included
- **Welcome page** with ByteQuest explorer artwork, branding and Start Adventure button. It appears on first launch; you can reopen it in More → Settings.
- **Home dashboard** with avatar, XP, continue learning and 6 colorful launch tiles.
- **Bottom navigation** with Home, Levels, Lessons, Quiz, More; active cyan glow/highlight and icons.
- **12 existing interactive puzzle levels**: binary, logic, code ordering, flowcharts, networking and hardware matching.
- **350 existing questions** in **seven subjects** (50 each); five 10-question packs per topic, feedback, explanations, lesson references and progress tracking.
- **Seven lessons**, **15 achievements**, **learning reports**, and **offline classroom challenge codes**; student profile and settings.
- **Made by MR.DEV** on welcome, main dashboard, and About page.

## Open in Android Studio
1. Extract the archive. Open **File → Open** and choose the `ByteQuest` folder containing `settings.gradle.kts`.
2. Allow Android Studio to sync the project. Use **JDK 17** and install **Android SDK Platform 34** if prompted. Recommended Gradle 8.9; AGP 8.7.3 and Kotlin 2.0.21 are configured in the project.
3. Connect a phone with USB debugging enabled or create an emulator (Android 7.0 / API 24 or later).
4. Choose `app` and click **Run**.
5. On first launch tap **START ADVENTURE**. Navigation and all game modes are offline.

### Gradle wrapper note
The source archive does not include a Gradle wrapper JAR or `gradlew` scripts. Android Studio can import and sync the Gradle build files using a locally available Gradle installation, or you can import the source into a fresh **Empty Views Activity** project. Do **not** expect `./gradlew` to work until the wrapper is generated. If making a new project, copy the entire `app/src/main/` folder, not just `MainActivity.kt` (the new `WelcomeActivity.kt` and drawable resources are required). Use the provided application ID `com.example.bytequest` or update package declarations.

## Classroom mode
Generated codes can be shared by text, but players enter them individually on their own devices. No online multiplayer, real-time sync, teacher server, accounts or cloud leaderboard are included. Quiz codes are deterministic, e.g. `BQQ-ALL-350`, `BQQ-NUM-50`; the older puzzle `BQ4` code format is still accepted.

## Verification / limitations
- Native-layout XML and resource reference checks passed.
- Kotlin source compiled against local **Android-shaped test stubs**; this does **not** prove that an actual Android APK compiles or runs.
- The original 350-question validator passed. Game rules remain unchanged.
- **A real Android SDK build, emulator run, and device-size/accessibility testing are still required.**
- The reference artwork is incorporated from the image provided in the conversation; obtain rights/permission for any third-party imagery before distributing through an app store.

## Project map
- `app/src/main/java/com/example/bytequest/WelcomeActivity.kt` — first launch
- `app/src/main/java/com/example/bytequest/MainActivity.kt` — tabs + all puzzle/quiz/student screens
- `app/src/main/java/com/example/bytequest/GameContent.kt` — lessons + puzzle levels
- `app/src/main/java/com/example/bytequest/QuizBank.kt` — all 350 questions
- `app/src/main/java/com/example/bytequest/ProgressStore.kt` — local persistence
- `app/src/main/res/layout` — welcome and game frame
- `app/src/main/res/drawable` — reference artwork, icons, background shapes and navigation
- `QUESTION_BANK_STUDENT.md`, `QUESTION_BANK_TEACHER_ANSWER_KEY.md`, `STUDENT_LESSON_GUIDE.md` — student and teacher resources

## Play Levels: 350-question expansion

The Play Levels tab retains the 12 interactive puzzles and now includes seven subject challenge cards. Each card launches all 50 questions or one of five 10-question levels. The Master Challenge launches all 350 questions. Answers, explanations, lesson references and saved quiz performance use the existing quiz engine.
