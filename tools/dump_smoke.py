#!/usr/bin/env python3
"""Report the client smoke test: print its log lines and screenshots (base64 JPEG) into the CI log.

The screenshots also go up as a workflow artifact; printing them lets them be read straight from the
job log, which is handy when artifacts cannot be downloaded.

    python3 tools/dump_smoke.py run
"""
from __future__ import annotations

import base64
import io
import sys
from pathlib import Path

CHUNK = 3000


def main() -> int:
    run = Path(sys.argv[1] if len(sys.argv) > 1 else "run")
    log = run / "logs" / "latest.log"
    text = log.read_text(encoding="utf-8", errors="replace") if log.exists() else ""

    print("----- SMOKE log lines -----")
    for line in text.splitlines():
        if "SMOKE" in line:
            print(line[:400])
    print("----- WARN/ERROR lines that mention Selarium -----")
    shown = 0
    for line in text.splitlines():
        if ("/WARN]" in line or "/ERROR]" in line) and "selarium" in line.lower() and shown < 80:
            print(line[:400])
            shown += 1

    shots = sorted((run / "smoke").glob("*.png"))
    try:
        from PIL import Image
    except ImportError:
        Image = None
    for shot in shots:
        data, ext = shot.read_bytes(), "png"
        if Image is not None:
            buffer = io.BytesIO()
            Image.open(shot).convert("RGB").save(buffer, "JPEG", quality=82)
            data, ext = buffer.getvalue(), "jpg"
        encoded = base64.b64encode(data).decode()
        parts = [encoded[i:i + CHUNK] for i in range(0, len(encoded), CHUNK)]
        for index, part in enumerate(parts, start=1):
            print(f"SMOKE_B64 {shot.stem}.{ext} {index}/{len(parts)} {part}")

    ok = "SMOKE_DONE" in text and "SMOKE_FAIL" not in text
    print(f"[dump_smoke] {len(shots)} screenshots, verdict: {'OK' if ok else 'FAILED'}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
