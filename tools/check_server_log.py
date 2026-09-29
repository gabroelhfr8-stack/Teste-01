#!/usr/bin/env python3
"""Scan a Forge server log for problems that matter and fail (exit 1) when found.

Ignored as benign: freshly created config files ("Incorrect key ... corrected from null") and the
missing server.properties of a first run. Everything else at ERROR/FATAL level that mentions Selarium,
datapack parsing or an exception is a failure.

Usage: python3 tools/check_server_log.py run/logs/latest.log
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

LEVEL = re.compile(r"/(WARN|ERROR|FATAL)\]")
IGNORE = re.compile(r"was corrected from null|Failed to load properties from file: server\.properties")
BAD = re.compile(r"selarium|Parsing error|Couldn't parse|Failed to load|Exception|Unable to load|Error loading|missing", re.I)


def main() -> int:
    path = Path(sys.argv[1] if len(sys.argv) > 1 else "run/logs/latest.log")
    if not path.exists():
        print(f"[check_server_log] {path} not found", file=sys.stderr)
        return 1
    warnings, failures = [], []
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        match = LEVEL.search(line)
        if not match or IGNORE.search(line):
            continue
        if match.group(1) in ("ERROR", "FATAL") and BAD.search(line):
            failures.append(line)
        else:
            warnings.append(line)
    print(f"[check_server_log] {len(warnings)} other warning/error lines, {len(failures)} failure(s)")
    for line in warnings[:40]:
        print("  note:", line[:220])
    for line in failures:
        print("  FAIL:", line[:400], file=sys.stderr)
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
