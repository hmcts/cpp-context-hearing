#!/usr/bin/env python3
"""
Create the reserve-a-slot tickets in Jira from reserve-a-slot-jira-import.csv.

Credentials come from YOUR environment. This script never stores or transmits them
anywhere except to your own Jira instance. Create a token at:
    https://id.atlassian.com/manage-profile/security/api-tokens

    export JIRA_URL=https://hmcts.atlassian.net
    export JIRA_USER=your.name@justice.gov.uk     # the account email
    export JIRA_TOKEN=...                         # the API token

Usage:
    python3 create_jira_tickets.py             # DRY RUN - shows what it would create
    python3 create_jira_tickets.py --first     # create ONE, so you can eyeball it
    python3 create_jira_tickets.py --go        # create the remainder

Every created key is appended to created-tickets.tsv, and the script skips anything
already listed there - so a re-run after a partial failure never duplicates.
Stdlib only: no jq, no pip install.
"""
import base64, csv, json, os, sys, urllib.request, urllib.error

CSV_PATH = os.environ.get("CSV", "reserve-a-slot-jira-import.csv")
PROJECT  = os.environ.get("PROJECT", "LPT")
LEDGER   = "created-tickets.tsv"
mode     = sys.argv[1] if len(sys.argv) > 1 else "--dry"

url   = os.environ.get("JIRA_URL", "").rstrip("/")
user  = os.environ.get("JIRA_USER", "")
token = os.environ.get("JIRA_TOKEN", "")
if mode != "--dry" and not all([url, user, token]):
    sys.exit("ERROR: set JIRA_URL, JIRA_USER and JIRA_TOKEN. See the header of this file.")

def read_rows(path):
    with open(path, encoding="utf-8") as f:
        rd = csv.reader(f)
        hdr = next(rd)
        label_ix = [i for i, c in enumerate(hdr) if c == "Labels"]
        ix = {c: i for i, c in enumerate(hdr) if c != "Labels"}
        for r in rd:
            yield {
                "ref":     r[ix["Internal Ref"]],
                "summary": r[ix["Summary"]],
                "type":    r[ix["Issue Type"]],
                "parent":  r[ix["Parent"]],
                "desc":    r[ix["Description"]],
                "labels":  [r[i] for i in label_ix if r[i]],
            }

done = {}
if os.path.exists(LEDGER):
    for line in open(LEDGER, encoding="utf-8"):
        p = line.rstrip("\n").split("\t")
        if len(p) >= 2:
            done[p[0]] = p[1]

rows = list(read_rows(CSV_PATH))
print(f"== {len(rows)} issues in {CSV_PATH} -> project {PROJECT} ==")
if mode == "--dry":
    print("== DRY RUN: nothing will be created. Then --first, then --go. ==\n")

created = failed = 0
for row in rows:
    ref = row["ref"]
    if ref in done:
        print(f"SKIP   {ref} (already created: {done[ref]})")
        continue
    if mode == "--dry":
        print(f"WOULD  {ref:8} [{row['type']:5}] {row['summary'][:70]}")
        continue

    payload = {"fields": {
        "project":   {"key": PROJECT},
        "summary":   row["summary"],
        "description": row["desc"],
        "issuetype": {"name": row["type"]},
        "labels":    row["labels"],
        "parent":    {"key": row["parent"]},
    }}
    req = urllib.request.Request(
        f"{url}/rest/api/2/issue",
        data=json.dumps(payload).encode(),
        headers={
            "Content-Type": "application/json",
            "Authorization": "Basic " + base64.b64encode(f"{user}:{token}".encode()).decode(),
        }, method="POST")
    try:
        with urllib.request.urlopen(req) as resp:
            key = json.load(resp)["key"]
        with open(LEDGER, "a", encoding="utf-8") as f:
            f.write(f"{ref}\t{key}\t{row['summary']}\n")
        print(f"OK     {ref:8} -> {key}")
        created += 1
        if mode == "--first":
            print(f"\nCreated one. Check {url}/browse/{key}, then run: python3 {sys.argv[0]} --go")
            sys.exit(0)
    except urllib.error.HTTPError as e:
        detail = e.read().decode(errors="replace")
        try:
            j = json.loads(detail)
            detail = json.dumps(j.get("errors") or j.get("errorMessages") or j)
        except Exception:
            detail = detail[:300]
        print(f"FAIL   {ref:8} HTTP {e.code}  {detail}")
        failed += 1
    except Exception as e:
        print(f"FAIL   {ref:8} {type(e).__name__}: {e}")
        failed += 1

if mode != "--dry":
    print(f"\ncreated={created} failed={failed}  (keys recorded in {LEDGER})")
