1) Template MD (Query-only)
# QUERY REVIEW — INDEX SCAN + SLOW PATTERN (AI OUTPUT)


## INPUT (MANDATORY)


### Query Under Review
- **Query ID:** `<free text>`
- **Query Type:** `SQL | FlexibleSearch`
- **Query Text:**
```sql
<paste query here>
DB / Engine Context

DB Vendor (if known): <MySQL | MariaDB | Postgres | Oracle | MSSQL | HANA | Unknown>

Hot Path Context: <request | cronjob | batch | OCC | promo | unknown>

Estimated Result Size: <small | medium | large | unknown>

Execution Plan Evidence (Optional but preferred)

If available, paste one:

EXPLAIN / EXPLAIN ANALYZE output

query plan text

monitoring plan snippet

<paste explain/plan here or leave empty>
Slow Query Patterns (MANDATORY)

Provide 1+ slow patterns below. AI MUST compare the query against them.

Pattern List:

Pattern ID: <PATTERN_1_NAME>
Pattern Query:

<slow pattern query>

Why slow (notes): <short notes>

Pattern ID: <PATTERN_2_NAME>
Pattern Query:

<slow pattern query>

Why slow (notes): <short notes>

TASKS (ONLY)
Task A — Index Scan Check

If execution plan evidence is provided:

Decide INDEX_SCAN | FULL_SCAN | MIXED | UNKNOWN

If no plan evidence:

Output likelihood only (HIGH/MEDIUM/LOW) with structural reasons

Never claim certainty without plan evidence.

Task B — Slow Pattern Similarity Check

Compare Query Under Review vs each slow pattern.

Mark match if query shares 2+ of:

same core join chain / same tables

DISTINCT + many joins

redundant join to same table

weak predicates / missing pagination

wide select columns

OUTPUT (MANDATORY)
A) Index Scan Result

Plan Evidence: PROVIDED | NOT_PROVIDED

Index Scan Result: INDEX_SCAN | FULL_SCAN | MIXED | UNKNOWN

Index Scan Likelihood: HIGH | MEDIUM | LOW (required when Plan Evidence = NOT_PROVIDED)

Reasoning: <bullet list of structural reasons>

Actions to Verify: <exact steps: run EXPLAIN with params, check indexes on join/where columns, etc>

B) Slow Pattern Match

For EACH pattern:

Pattern ID: <id>

Similar: YES | NO | UNKNOWN

Similarity Reasons: <mapping: joins/where/distinct/columns>

Risk Level: LOW | MEDIUM | HIGH

Recommendation: <rewrite / reduce joins / remove redundant join / add pagination / add index / split query / caching>

C) Final Verdict

Overall Risk: LOW | MEDIUM | HIGH

One-line Summary: <short>

RULES FOR AI (STRICT)

No hallucination: if plan evidence is not provided, do NOT claim actual scan type.

Be concrete: name join columns/predicates causing scan risk.

Keep scope: do not review runtime exceptions / memory / general performance outside query.
