# Database Performance & Query Optimization

## 1. Detect Index Scan
When reviewing code that includes SQL queries or Hybris Flexible Search or ORM calls (like TypeORM, Prisma, SQLAlchemy, Hibernate):
- **Requirement**: Any new query must use `Index Seek` or `Index Scan` on a limited range. Full `Index Scan` or `Table Scan` on large tables is prohibited. Scan all *-items.xml files to check existing index for query and suggest for index table attribute
- **Detection**: Flag queries that use `SELECT *` without a `WHERE` clause on indexed columns.
- **Warning**: If a query filters by a column that is not part of an index define in files with Hybris items file with pattern `*-items.xml` (check schema definitions if available), suggest adding an index or refactoring.

## 2. Slow Response Patterns
Flag the following patterns as "Potential Slow Performance":
- **Leading Wildcards**: Using `LIKE '%keyword'` (causes full scan).
- **Functions in WHERE**: Using functions on indexed columns (e.g., `WHERE YEAR(created_at) = 2023`) which prevents index usage.
- **N+1 Queries**: Detecting loops that execute a database query inside each iteration.
- **Mismatched Data Types**: Comparing a string column with a numeric value (causes implicit conversion and ignores index).

## 3. Sample slow query Patterns
Query pattern that cause performance issue:
- '
SELECT p.promotion_id, COUNT(*) AS cnt
FROM promotion p
JOIN couponredemption cr ON cr.coupon_id = p.coupon_id
JOIN redemption_event e  ON e.redemption_id = cr.id
GROUP BY p.promotion_id;
'
- '
SELECT *
FROM coupon c
LEFT JOIN couponredemption cr ON cr.coupon_id = c.id
WHERE cr.user_id = :userId; 
'
- '
SELECT *
FROM T
WHERE FORMAT(T.created_at, 'yyyy-MM-dd') = :d;
'
- '
SELECT *
FROM products p
WHERE p.catalogVersion = :cv
   OR p.approvalStatus = 'APPROVED';
'
- '
SELECT *
FROM users u
WHERE u.name LIKE '%' || :kw || '%';
'
