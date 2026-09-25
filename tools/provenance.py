#!/usr/bin/env python3
"""Generate exact file provenance against the pinned Grim tree; no Git mutations."""
from pathlib import Path
import hashlib
import subprocess

root = Path(__file__).resolve().parents[1]
base = (root / 'docs/UPSTREAM_COMMIT.txt').read_text().strip()
tracked = subprocess.check_output(['git', 'ls-tree', '-r', base], cwd=root, text=True)
rows = []
for line in tracked.splitlines():
    meta, name = line.split('\t', 1)
    p = root / name
    blob = meta.split()[2]
    current = subprocess.check_output(['git', 'hash-object', str(p)], cwd=root, text=True).strip() if p.is_file() else '-'
    rows.append((name, 'unchanged' if current == blob else 'modified' if p.is_file() else 'removed/moved', blob, current))
output = root / 'release/SOURCE_PROVENANCE.tsv'
output.write_text('path\tstatus\tupstream_git_blob\tcurrent_git_blob\n' + ''.join('\t'.join(r)+'\n' for r in rows))
new = subprocess.check_output(['git', 'ls-files', '--others', '--exclude-standard'], cwd=root, text=True).splitlines()
# Files already committed after the base are also original fork additions.
new += subprocess.check_output(['git','diff','--name-only','--diff-filter=A',base], cwd=root, text=True).splitlines()
additions = sorted(set(new) - {r[0] for r in rows})
with output.open('a') as f:
    for name in additions:
        if name == 'release/SOURCE_PROVENANCE.tsv': continue
        p = root / name
        if p.is_file():
            current = subprocess.check_output(['git','hash-object',str(p)],cwd=root,text=True).strip()
            f.write(f'{name}\tfork-added\t-\t{current}\n')
print(f'Wrote {output}')
