"""Run: python verify_question_bank.py  (no external dependencies)."""
import json
from collections import Counter
from pathlib import Path
import re

root=Path(__file__).resolve().parent
q=json.loads((root/'QUIZ_350_QUESTION_BANK.json').read_text(encoding='utf-8'))
topics=('NUM','LOG','PRG','DBG','NET','ALG','HWR')
max_idea={'NUM':11,'LOG':9,'PRG':11,'DBG':11,'NET':11,'ALG':12,'HWR':10}
assert len(q)==350
assert Counter(x['topic'] for x in q)==Counter({t:50 for t in topics})
assert len({x['id'] for x in q})==350
assert len({x['question'].casefold() for x in q})==350
for t in topics:
    assert {x['id'] for x in q if x['topic']==t}=={f'{t}{i:02d}' for i in range(1,51)}
for question in q:
    assert len(question['options'])==len(set(question['options']))==4
    assert 0<=question['correct']<=3
    assert 0<=question['referencePoint']<max_idea[question['topic']]
    assert question['question'].strip() and question['explanation'].strip()
for filename in ['QUESTION_BANK_STUDENT.md','QUESTION_BANK_TEACHER_ANSWER_KEY.md']:
    doc=(root/filename).read_text(encoding='utf-8')
    headings=re.findall(r'^### ((?:NUM|LOG|PRG|DBG|NET|ALG|HWR)\d\d)\. ',doc,re.M)
    assert len(headings)==350 and set(headings)=={x['id'] for x in q},filename
assert '**Correct answer:' not in (root/'QUESTION_BANK_STUDENT.md').read_text(encoding='utf-8')
assert (root/'QUESTION_BANK_TEACHER_ANSWER_KEY.md').read_text(encoding='utf-8').count('**Correct answer:')==350
print('PASS: exactly 350 questions, 50 in each subject, valid choices and numbered lesson references, 350 entries in student and teacher guides.')
