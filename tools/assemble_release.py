#!/usr/bin/env python3
"""Package exact source, built Bukkit JAR, test reports and SHA256 checksums.
Usage: python3 tools/assemble_release.py /path/to/built/source/tree
Run the build and notice/source inventory before this script. No publication.
"""
from pathlib import Path
import hashlib, shutil, subprocess, sys, zipfile
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
built=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else root
version=(root/'release/VERSION_NUMBER.txt').read_text().strip()
release=root/'release';release.mkdir(exist_ok=True)
jar=built/f'bukkit/build/libs/defender-bukkit-{version}.jar'
if not jar.is_file():raise SystemExit('Missing release build JAR; use ./gradlew build -Prelease=true')
shutil.copyfile(jar,release/jar.name)
reports=release/'test-results';reports.mkdir(exist_ok=True)
for module in ['common','defender-core']:
    for p in (built/module/'build/test-results/test').glob('TEST-*.xml'):
        result=ET.parse(p).getroot()
        if int(result.attrib['failures']) or int(result.attrib['errors']):raise SystemExit('Failing tests: '+str(p))
        shutil.copyfile(p,reports/(module+'-'+p.name))
paths=subprocess.check_output(['git','ls-files','--cached','--others','--exclude-standard'],cwd=root,text=True).splitlines()
source=release/f'defender-{version}-source.zip'
with zipfile.ZipFile(source,'w',compression=zipfile.ZIP_DEFLATED) as z:
    for name in sorted(set(paths)):
        if name.startswith('release/') or not (root/name).is_file():continue
        if '/build/' in name or name.startswith('.git/'):continue
        z.write(root/name,name)
    for name in ['ATTRIBUTION.md','SOURCE_CHANGES.md','THIRD_PARTY.md','VERSION_NUMBER.txt']:
        z.write(release/name,'release/'+name)
rows=[]
for p in sorted(release.rglob('*')):
    if p.is_file() and p.name!='SHA256SUMS' and p.suffix in ('.jar','.zip'):
        rows.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.relative_to(release).as_posix())
(release/'SHA256SUMS').write_text('\n'.join(rows)+'\n')
print('Packaged',jar.name,'and source; dependency sources included separately. Review publication checklist.')
