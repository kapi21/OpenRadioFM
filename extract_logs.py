
import json
import sys

log_file = sys.argv[1]
with open(log_file, 'r', encoding='utf-8') as f:
    for line in f:
        try:
            entry = json.loads(line)
            tag = entry.get('tag', '')
            msg = entry.get('message', '')
            if 'QS6Engine' in tag or 'com.example.openradiofm' in entry.get('applicationId', ''):
                print(f"[{tag}] {msg}")
        except:
            pass
