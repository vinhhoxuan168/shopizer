# Database Performance & Query Optimization

## 1. Detect Index Scan
When reviewing code that includes SQL queries or Hybris Flexible Search or ORM calls (like TypeORM, Prisma, SQLAlchemy, Hibernate):
- **Requirement**: Any new query must use `Index Seek` or `Index Scan` on a limited range. Full `Index Scan` or `Table Scan` on large tables is prohibited. Scan all *-items.xml files to check existing index for query and suggest for index attribute
- **Detection**: Flag queries that use `SELECT *` without a `WHERE` clause on indexed columns.
- **Warning**: If a query filters by a column that is not part of an index (check schema definitions if available), suggest adding an index or refactoring.

## 2. Slow Response Patterns
Flag the following patterns as "Potential Slow Performance":
- **Leading Wildcards**: Using `LIKE '%keyword'` (causes full scan).
- **Functions in WHERE**: Using functions on indexed columns (e.g., `WHERE YEAR(created_at) = 2023`) which prevents index usage.
- **N+1 Queries**: Detecting loops that execute a database query inside each iteration.
- **Mismatched Data Types**: Comparing a string column with a numeric value (causes implicit conversion and ignores index).

## 3. Sample slow query Patterns
Query pattern that cause performance issue
'SELECT accountId, siebelAcctId, threshold, SUM(isUsed) as orderedAmt FROM (SELECT item_t6.p_increasememberaccountid as accountId, item_t12.p_siebelacctid AS siebelAcctId, item_t12.p_threshold AS threshold, CASE WHEN item_t4.PK IS NULL THEN '***' ELSE '***' END as isUsed FROM is32promotion item_t0 JOIN is32promotiontag item_t1 ON item_t0.p_promotiontag = item_t1.PK JOIN coupon item_t2 ON item_t2.p_couponid = item_t0.p_redeemdigitalcoupon LEFT JOIN couponredemption item_t3 ON item_t2.PK = item_t3.p_coupon LEFT JOIN users item_t4 ON ( item_t3.p_user = item_t4.PK AND item_t4.PK = ?) JOIN enumerationvalues8e item_t5 ON item_t1.p_elabpromotiondisplaytype = item_t5.PK JOIN is32reward item_t6 ON item_t0.p_uid = item_t6.p_promotionuid JOIN enumerationvalues8e item_t7 ON item_t6.p_rewardtype = item_t7.PK JOIN is32promotionactivity item_t8 ON item_t8.p_promotionuid = item_t0.p_uid JOIN is32bucket item_t9 ON item_t0.p_uid = item_t9.p_promotionuid JOIN is32promoitem item_t10 ON item_t10.p_bucketuid = item_t9.uniqueid JOIN products item_t11 ON item_t11.p_code = item_t10.p_itemcode JOIN estamptier item_t12 ON item_t6.p_increasememberaccountid = item_t12.p_accountid WHERE (( item_t5.Code = '***') AND item_t7.Code = '***' AND item_t0.p_status = '***' and item_t0.p_suspended = '***' AND item_t11.p_catalogversion = ? AND item_t0.p_startdate <= ? AND item_t0.p_enddate > ? AND FORMAT( item_t8.p_starttime , '***') <= FORMAT(?, '***') AND FORMAT( item_t8.p_endtime , '***') > FORMAT(?, '***')) AND ((item_t0.TypePkString=? AND item_t1.TypePkString=? AND item_t2.TypePkString IN (?,?,..., ?) AND (item_t3.TypePkString IS NULL OR ( item_t3.TypePkString=? ) ) AND (item_t4.TypePkString IS NULL OR item_t4.TypePkString IN (?,?,..., ?)) AND item_t5.TypePkString=? AND item_t6.TypePkString=? AND item_t7.TypePkString=? AND item_t8.TypePkString=? AND item_t9.TypePkString=? AND item_t10.TypePkString=? AND item_t11.TypePkString IN (?,?,..., ?) AND ((( item_t11.p_onlinedate IS NULL OR item_t11.p_onlinedate <= ?) AND ( item_t11.p_offlinedate IS NULL OR item_t11.p_offlinedate >= ?)) AND ( item_t11.p_catalogversion IN (?,?,..., ?)) AND ( item_t11.p_approvalstatus = '***' )) AND item_t12.TypePkString=? ))) accountQuota GROUP BY accountId, siebelAcctId, threshold order by accountId'
