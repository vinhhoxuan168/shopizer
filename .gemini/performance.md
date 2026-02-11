# Database Performance & Query Optimization

## 1. Detect Index Scan
When reviewing code that includes SQL queries or ORM calls (like TypeORM, Prisma, SQLAlchemy, Hibernate):
- **Requirement**: Any new query must use `Index Seek` or `Index Scan` on a limited range. Full `Index Scan` or `Table Scan` on large tables is prohibited.
- **Detection**: Flag queries that use `SELECT *` without a `WHERE` clause on indexed columns.
- **Warning**: If a query filters by a column that is not part of an index (check schema definitions if available), suggest adding an index or refactoring.

## 2. Slow Response Patterns
Flag the following patterns as "Potential Slow Performance":
- **Leading Wildcards**: Using `LIKE '%keyword'` (causes full scan).
- **Functions in WHERE**: Using functions on indexed columns (e.g., `WHERE YEAR(created_at) = 2023`) which prevents index usage.
- **N+1 Queries**: Detecting loops that execute a database query inside each iteration.
- **Mismatched Data Types**: Comparing a string column with a numeric value (causes implicit conversion and ignores index).
