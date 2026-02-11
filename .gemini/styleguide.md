**Query risk check (Index Scan + Slow Pattern)**

- Index scan evidence: `NO` → Likelihood: `HIGH/MEDIUM/LOW`
- Clause causing scan risk: `<e.g., FORMAT(col), LIKE '%x', OR, function on join key>`
- Slow pattern match: `P? YES/NO` (reason: `<join chain / DISTINCT / wide select / redundant join>`)
- Suggested rewrite (1-2 lines): `<make predicate sargable / remove redundant join / add LIMIT>`
