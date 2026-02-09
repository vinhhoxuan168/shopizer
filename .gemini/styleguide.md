## Query Review Add-on: Index Scan & Slow Query Pattern Match (MANDATORY)

This section applies when reviewing SQL/FlexibleSearch queries.

### A) Index Scan Check (Evidence-first)

#### Rule A1 — Evidence Required for Certainty
You MUST NOT claim "uses index scan" / "full table scan" as a fact unless an execution plan is provided.
Valid evidence:
- `EXPLAIN` / `EXPLAIN ANALYZE` output
- DB execution plan screenshot/text
- Dynatrace/DB monitoring showing plan/hash

If evidence is NOT provided, you must output a likelihood assessment only.

#### Output (MANDATORY)
For each reviewed query, output:

- **Index Scan Evidence:** `PROVIDED | NOT_PROVIDED`
- **Index Scan Result:** `INDEX_SCAN | FULL_SCAN | UNKNOWN`  
  (Use `UNKNOWN` when NOT_PROVIDED)
- **Index Scan Likelihood:** `HIGH | MEDIUM | LOW` (required when NOT_PROVIDED)
- **Reasoning:** `<concrete reasons based on query structure>`
- **Action:** `<exact steps to verify/improve>`

#### Heuristics for Likelihood (when NOT_PROVIDED)
Increase FULL_SCAN risk if any of the following is present:
- Function applied on indexed column in WHERE/JOIN: `LOWER(col)`, `DATE(col)`, `CAST(col)`, `SUBSTR(col)`, etc.
- Leading wildcard: `LIKE '%abc'`
- OR conditions across different columns without supporting indexes
- Missing/ineffective WHERE predicates (broad filter)
- Join keys are not PK/unique or look non-indexed (code/uid without index)
- Large LEFT JOIN chains + DISTINCT + wide SELECT
- ORDER BY on non-indexed column, especially with LIMIT missing
- Query filters on low-selectivity column only (status=1 without other filters)
- Subqueries in hot path

Suggested Actions:
- Ask for `EXPLAIN` plan for the exact query with representative params
- Check indexes for join columns + where predicates
- Rewrite predicates to avoid functions on columns
- Add pagination / LIMIT where applicable
- Reduce join breadth, remove redundant joins, narrow selected columns

---

### B) Slow Query Pattern Match (Template-driven)

#### Rule B1 — Slow Query Templates Input
If the review template provides "Slow Query Patterns" (reference queries), you MUST compare the PR query against them.

#### Output (MANDATORY)
For each reviewed query, output:

- **Slow Pattern Evidence:** `PROVIDED | NOT_PROVIDED`
- **Similar To Slow Pattern:** `YES | NO | UNKNOWN`
- **Matched Pattern ID:** `<pattern name/id>` or `N/A`
- **Similarity Reason:** `<concrete mapping: joins/where/group/distinct>`
- **Risk Level:** `LOW | MEDIUM | HIGH`
- **Recommendation:** `<rewrite / index / split query / caching / pagination>`

If patterns are NOT provided:
- **Slow Pattern Evidence:** `NOT_PROVIDED`
- **Similar To Slow Pattern:** `UNKNOWN`
- Provide generic risk assessment only.

#### Similarity Heuristics (practical)
Mark `Similar To Slow Pattern = YES` if query shares 2+ of:
- Same core join chain (same tables/items and join direction)
- Same anti-pattern(s): `DISTINCT` with many joins, redundant join to same table, missing restrictive predicates, broad time-range filter, etc.
- Same high-cardinality join point (e.g., relation table -> product -> promotion -> tag)
- Same “select wide columns” with DISTINCT
- Same predicate structure (e.g., status + date range + catalogVersion)

---

### C) FlexibleSearch-specific Notes (Hybris)
- FlexibleSearch plans are DB-dependent; you still need DB `EXPLAIN` for certainty.
- Always record:
  - involved typecodes/items
  - join keys (pk vs code/uid)
  - whether query returns large result set (missing pagination)
