from pathlib import Path
import re, xml.etree.ElementTree as ET
p=Path(__file__).parent
main=p/'app/src/main'
xmls=list(main.rglob('*.xml'))
for f in xmls: ET.parse(f)
kt=(main/'java/com/example/bytequest/MainActivity.kt').read_text()
welcome=(main/'java/com/example/bytequest/WelcomeActivity.kt').read_text()
layout=(main/'res/layout/activity_main.xml').read_text()
manifest=(main/'AndroidManifest.xml').read_text()
assert all(x in layout for x in ['tabHome','tabLevels','tabLearn','tabQuiz','tabMore','contentHost','contentScroll'])
assert 'WelcomeActivity' in manifest and 'START ADVENTURE' in (main/'res/layout/activity_welcome.xml').read_text()
assert 'Made by MR.DEV' in welcome or 'Made by MR.DEV' in (main/'res/layout/activity_welcome.xml').read_text()
assert all(x in kt for x in ['renderHome()','renderLevels()','renderLearn()','renderQuiz()','renderProgress()','renderBadges()','renderClass()','renderSettings()','renderProfile()'])
assert all((main/'res/drawable'/f).exists() for f in ['student_avatar.png','welcome_art.jpg','bytequest_icon.png'])
assert (p/'QUIZ_350_QUESTION_BANK.json').exists()
print('PASS:',len(xmls),'well-formed XML files, five navigation tabs, ten screens, all image resources.')
