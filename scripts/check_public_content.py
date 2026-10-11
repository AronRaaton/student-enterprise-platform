"""Heuristic public-content audit. Prints locations and categories, never matched values.
Run from any directory: python scripts/check_public_content.py
Does not replace a dedicated secret scanner or a manual review.
"""
from pathlib import Path
import re, sys, os
ROOT = Path(__file__).resolve().parents[1]
EXCLUDED = {'.git', 'node_modules', 'target', 'dist', '.build-tools', 'uploads', 'logs', '__pycache__'}
TEXT = {'.java', '.vue', '.js', '.cjs', '.json', '.xml', '.properties', '.sql', '.md', '.html', '.css', '.py', '.example', '.cmd', '.ps1'}
PATTERNS = {
    'private-key': r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----',
    'github-token': r'\b(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{50,})\b',
    'aws-access-key': r'\b(?:AKIA|ASIA)[A-Z0-9]{16}\b',
    'jwt-literal': r'\beyJ[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{15,}',
    'private-absolute-path': r'(?:[A-Za-z]:[\\/](?:Users|StudentAndEnter|Portfolio)[\\/]|/(?:home|Users)/[\w.-]+/)',
    'literal-config-secret': r'^\s*(?:spring\.datasource\.password|spring\.mail\.password|flyway\.password)\s*=\s*(?!\$\{|$).+',
}
findings = []
count = 0
def public_files():
    for parent, dirs, names in os.walk(ROOT):
        dirs[:] = sorted(d for d in dirs if d not in EXCLUDED)
        for name in sorted(names):
            yield Path(parent) / name

for p in public_files():
    rel = p.relative_to(ROOT)
    if not p.is_file() or any(x in EXCLUDED for x in rel.parts) or p.suffix == '.log': continue
    if p.suffix not in TEXT and p.name not in {'.gitignore','mvnw'}: continue
    count += 1
    text = p.read_text(encoding='utf-8-sig', errors='replace')
    for n, line in enumerate(text.splitlines(), 1):
        for category, pattern in PATTERNS.items():
            if re.search(pattern, line): findings.append((str(rel), n, category))
        # Contact-like values in application sources: fixtures must use reserved domains.
        if rel.parts[0] in {'src', 'StudentAndEnterFront', 'database'} and 'package-lock.json' not in str(rel):
            for email in re.findall(r'[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}', line):
                if not email.endswith(('@example.invalid','@example.com','@example.org')):
                    findings.append((str(rel), n, 'review-contact'))
for path, line, category in findings:
    print(f'{path}:{line}: {category} [value withheld]')
print(f'Scanned {count} public text files; {len(findings)} findings.')
sys.exit(1 if findings else 0)
