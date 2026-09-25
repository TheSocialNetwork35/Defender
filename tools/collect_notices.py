#!/usr/bin/env python3
"""Copy embedded dependency license/notice texts to collision-free paths."""
from pathlib import Path
import csv, zipfile, hashlib, re, sys
root = Path(sys.argv[1]) if len(sys.argv)>1 else Path(__file__).resolve().parents[1]
cache = Path.home()/'.gradle/caches/modules-2/files-2.1'
report=[]
for row in csv.DictReader((root/'release/DEPENDENCIES.tsv').open(), delimiter='\t'):
    coordinate=row['coordinate']; parts=coordinate.split(':')
    if len(parts)!=3: continue
    group,name,version=parts
    candidates=list((cache/group/name/version).glob('*/'+row['filename']))
    candidates=[p for p in candidates if hashlib.sha256(p.read_bytes()).hexdigest()==row['sha256']]
    if not candidates: report.append(coordinate+'\tPROJECT_OR_MISSING');continue
    jar=candidates[0];count=0
    with zipfile.ZipFile(jar) as z:
        for item in z.infolist():
            base=Path(item.filename).name.lower()
            if item.is_dir() or item.file_size>1024*1024: continue
            if re.match(r'^(license|licence|notice|copying)([._-].*)?$',base):
                safe=re.sub(r'[^a-zA-Z0-9._-]','_',item.filename)
                p=root/'release/third-party-notices'/coordinate.replace(':','_')/safe
                p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(item));count+=1
    report.append(coordinate+'\t'+str(count)+' embedded notices')
(root/'release/NOTICE_AUDIT.tsv').write_text('\n'.join(report)+'\n')
