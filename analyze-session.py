#!/usr/bin/env python3
"""
Claude Code 세션 로그(JSONL) 분석 스크립트

사용법:
  # 터미널 출력만
  python3 analyze-session.py <session.jsonl>

  # results 파일에 자동 기록
  python3 analyze-session.py <session.jsonl> --save clean 1
  python3 analyze-session.py <session.jsonl> --save messy 2

  # 최근 세션 자동 탐지 (프로젝트 디렉토리 기준)
  python3 analyze-session.py --latest CleanCode --save clean 1
  python3 analyze-session.py --latest messyCode --save messy 3

세션 파일 위치:
  ls ~/.claude/projects/*/*.jsonl
"""

import json
import sys
import re
import os
from pathlib import Path
from collections import defaultdict
from datetime import datetime

SCRIPT_DIR = Path(__file__).parent.resolve()
RESULTS_DIR = SCRIPT_DIR / "results"

PHASE_NAMES = {
    1: "프로젝트 초기 세팅 + 게시글 CRUD",
    2: "댓글 기능",
    3: "게시글 목록 + 검색",
    4: "좋아요 기능",
    5: "비밀번호 변경 기능 (기능 변경)",
    6: "대댓글 기능 (기능 변경)",
    7: "정렬 옵션 (기능 변경)",
}


def find_latest_session(project_name: str) -> str:
    """~/.claude/projects/ 에서 project_name을 포함하는 가장 최근 JSONL 파일을 찾는다."""
    claude_projects = Path.home() / ".claude" / "projects"
    if not claude_projects.exists():
        print(f"Error: {claude_projects} not found")
        sys.exit(1)

    candidates = []
    for project_dir in claude_projects.iterdir():
        if project_name.lower() in project_dir.name.lower():
            for jsonl_file in project_dir.glob("*.jsonl"):
                candidates.append(jsonl_file)

    if not candidates:
        print(f"Error: No session files found for project '{project_name}'")
        print(f"Available projects:")
        for d in claude_projects.iterdir():
            if d.is_dir():
                print(f"  {d.name}")
        sys.exit(1)

    # Sort by modification time, return most recent
    candidates.sort(key=lambda f: f.stat().st_mtime, reverse=True)
    return str(candidates[0])


def analyze_session(jsonl_path: str) -> dict:
    entries_by_msg_id = {}
    all_entries = []

    with open(jsonl_path, "r") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                entry = json.loads(line)
            except json.JSONDecodeError:
                continue

            message = entry.get("message", {})
            msg_id = message.get("id")

            if msg_id and entry.get("type") == "assistant":
                entries_by_msg_id[msg_id] = entry
            else:
                all_entries.append(entry)

    final_entries = all_entries + list(entries_by_msg_id.values())

    metrics = {
        "input_tokens": 0,
        "output_tokens": 0,
        "cache_creation_input_tokens": 0,
        "cache_read_input_tokens": 0,
        "api_calls": 0,
        "user_messages": 0,
        "assistant_messages": 0,
        "tool_calls": 0,
        "tool_types": defaultdict(int),
    }

    for entry in final_entries:
        msg_type = entry.get("type")
        message = entry.get("message", {})
        usage = message.get("usage", {})

        if usage and msg_type == "assistant":
            inp = usage.get("input_tokens", 0)
            out = usage.get("output_tokens", 0)
            cache_create = usage.get("cache_creation_input_tokens", 0)
            cache_read = usage.get("cache_read_input_tokens", 0)

            if out > 0 or inp > 0:
                metrics["input_tokens"] += inp
                metrics["output_tokens"] += out
                metrics["cache_creation_input_tokens"] += cache_create
                metrics["cache_read_input_tokens"] += cache_read
                metrics["api_calls"] += 1

        if msg_type == "user":
            metrics["user_messages"] += 1
        elif msg_type == "assistant":
            metrics["assistant_messages"] += 1

        content = message.get("content", [])
        if isinstance(content, list):
            for block in content:
                if isinstance(block, dict) and block.get("type") == "tool_use":
                    metrics["tool_calls"] += 1
                    tool_name = block.get("name", "unknown")
                    metrics["tool_types"][tool_name] += 1

    return metrics


def print_report(metrics: dict, filepath: str):
    session_id = Path(filepath).stem
    total_input = (
        metrics["input_tokens"]
        + metrics["cache_creation_input_tokens"]
        + metrics["cache_read_input_tokens"]
    )
    grand_total = total_input + metrics["output_tokens"]

    print(f"\n{'='*60}")
    print(f"  Session: {session_id}")
    print(f"  File:    {filepath}")
    print(f"{'='*60}")

    print(f"\n## Token Usage")
    print(f"  Input Tokens (uncached): {metrics['input_tokens']:>12,}")
    print(f"  Cache Creation:          {metrics['cache_creation_input_tokens']:>12,}")
    print(f"  Cache Read:              {metrics['cache_read_input_tokens']:>12,}")
    print(f"  {'─'*44}")
    print(f"  Total Input:             {total_input:>12,}")
    print(f"  Output Tokens:           {metrics['output_tokens']:>12,}")
    print(f"  {'─'*44}")
    print(f"  Grand Total:             {grand_total:>12,}")

    print(f"\n## Activity")
    print(f"  API Calls:               {metrics['api_calls']:>12,}")
    print(f"  User Messages:           {metrics['user_messages']:>12,}")
    print(f"  Assistant Messages:      {metrics['assistant_messages']:>12,}")
    print(f"  Tool Calls:              {metrics['tool_calls']:>12,}")

    if metrics["tool_types"]:
        print(f"\n## Tool Usage Breakdown")
        for tool, count in sorted(
            metrics["tool_types"].items(), key=lambda x: -x[1]
        ):
            print(f"  {tool:<25} {count:>6,}")
    print()


def save_to_results(metrics: dict, project: str, phase: int):
    """results/clean.md 또는 results/messy.md의 해당 Phase 섹션에 결과를 기록한다."""
    results_file = RESULTS_DIR / f"{project}.md"
    if not results_file.exists():
        print(f"Error: {results_file} not found")
        sys.exit(1)

    total_input = (
        metrics["input_tokens"]
        + metrics["cache_creation_input_tokens"]
        + metrics["cache_read_input_tokens"]
    )
    grand_total = total_input + metrics["output_tokens"]

    content = results_file.read_text()

    phase_name = PHASE_NAMES.get(phase, f"Phase {phase}")
    # Find the phase section and replace the table
    # Pattern: look for the phase header, then replace the table after it
    section_header = f"## Phase {phase}: {phase_name}"

    if section_header not in content:
        print(f"Error: Section '{section_header}' not found in {results_file}")
        print(f"Available phases: {list(PHASE_NAMES.keys())}")
        sys.exit(1)

    tool_breakdown = ", ".join(
        f"{t}:{c}" for t, c in sorted(metrics["tool_types"].items(), key=lambda x: -x[1])
    )

    new_table = f"""| Metric              | Value |
|---------------------|-------|
| Input Tokens        | {metrics['input_tokens']:,} |
| Output Tokens       | {metrics['output_tokens']:,} |
| Total Tokens        | {grand_total:,} |
| Cache Creation      | {metrics['cache_creation_input_tokens']:,} |
| Cache Read          | {metrics['cache_read_input_tokens']:,} |
| Tool Calls          | {metrics['tool_calls']} |
| Conversation Turns  | {metrics['user_messages']} |
| Build Success       | Y/N   |
| API Test Pass       | Y/N   |
| Manual Fix Required | Y/N   |
| Notes               | tools: {tool_breakdown} |"""

    # Replace the existing table in the section
    # Find start of this section's table and end (next ## or end of file)
    section_start = content.index(section_header)
    next_section = content.find("\n## ", section_start + len(section_header))
    if next_section == -1:
        next_section = len(content)

    section_content = content[section_start:next_section]

    # Replace the table within this section
    table_pattern = r"\| Metric.*?\| Notes\s*\|[^\n]*"
    new_section = re.sub(table_pattern, new_table, section_content, flags=re.DOTALL)

    content = content[:section_start] + new_section + content[next_section:]
    results_file.write_text(content)

    print(f"Results saved to {results_file} (Phase {phase}: {phase_name})")
    print(f"  → Build Success / API Test Pass / Manual Fix Required 는 직접 Y/N 기입해주세요")


def update_totals(project: str):
    """results 파일의 Total Summary 섹션을 Phase 1~4 합산으로 갱신한다."""
    results_file = RESULTS_DIR / f"{project}.md"
    content = results_file.read_text()

    totals = {
        "input": 0, "output": 0, "total": 0,
        "tool_calls": 0, "turns": 0,
    }

    for phase in range(1, 5):
        phase_name = PHASE_NAMES.get(phase, "")
        header = f"## Phase {phase}: {phase_name}"
        if header not in content:
            continue

        section_start = content.index(header)
        next_section = content.find("\n## ", section_start + len(header))
        section = content[section_start:next_section] if next_section != -1 else content[section_start:]

        for line in section.split("\n"):
            if "| Input Tokens" in line:
                val = re.search(r"\|\s*([\d,]+)\s*\|", line.split("|")[2])
                if val:
                    totals["input"] += int(val.group(1).replace(",", ""))
            elif "| Output Tokens" in line:
                val = re.search(r"\|\s*([\d,]+)\s*\|", line.split("|")[2])
                if val:
                    totals["output"] += int(val.group(1).replace(",", ""))
            elif "| Total Tokens" in line:
                val = re.search(r"\|\s*([\d,]+)\s*\|", line.split("|")[2])
                if val:
                    totals["total"] += int(val.group(1).replace(",", ""))
            elif "| Tool Calls" in line:
                val = re.search(r"\|\s*(\d+)\s*\|", line.split("|")[2])
                if val:
                    totals["tool_calls"] += int(val.group(1))
            elif "| Conversation Turns" in line:
                val = re.search(r"\|\s*(\d+)\s*\|", line.split("|")[2])
                if val:
                    totals["turns"] += int(val.group(1))

    # Update Total Summary section
    summary_header = "## Total Summary"
    if summary_header in content:
        summary_start = content.index(summary_header)
        new_summary = f"""## Total Summary

| Metric              | Value |
|---------------------|-------|
| Total Input Tokens  | {totals['input']:,} |
| Total Output Tokens | {totals['output']:,} |
| Total Tokens        | {totals['total']:,} |
| Total Tool Calls    | {totals['tool_calls']} |
| Total Turns         | {totals['turns']} |
"""
        content = content[:summary_start] + new_summary
        results_file.write_text(content)
        print(f"Total summary updated in {results_file}")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python3 analyze-session.py <session.jsonl>")
        print("  python3 analyze-session.py <session.jsonl> --save <clean|messy> <phase>")
        print("  python3 analyze-session.py --latest <CleanCode|messyCode> --save <clean|messy> <phase>")
        print("")
        print("Examples:")
        print("  python3 analyze-session.py --latest CleanCode --save clean 1")
        print("  python3 analyze-session.py --latest messyCode --save messy 2")
        print("")
        print("Find session files:")
        print("  ls ~/.claude/projects/*/*.jsonl")
        sys.exit(1)

    # Parse arguments
    filepath = None
    save_project = None
    save_phase = None

    args = sys.argv[1:]
    i = 0
    while i < len(args):
        if args[i] == "--latest":
            i += 1
            filepath = find_latest_session(args[i])
        elif args[i] == "--save":
            i += 1
            save_project = args[i]
            i += 1
            save_phase = int(args[i])
        else:
            filepath = args[i]
        i += 1

    if not filepath:
        print("Error: No session file specified")
        sys.exit(1)

    if not Path(filepath).exists():
        print(f"Error: File not found: {filepath}")
        sys.exit(1)

    metrics = analyze_session(filepath)
    print_report(metrics, filepath)

    if save_project and save_phase:
        save_to_results(metrics, save_project, save_phase)
        update_totals(save_project)
