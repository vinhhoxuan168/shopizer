# Database Performance & Query Optimization

## 1. Detect Index Scan
When reviewing code that includes SQL queries or Hybris Flexible Search or ORM calls (like TypeORM, Prisma, SQLAlchemy, Hibernate):
- **Requirement**: Any new query must use `Index Seek` or `Index Scan` on a limited range. Full `Index Scan` or `Table Scan` on large tables is prohibited. Scan all *-items.xml files to check existing index for query and suggest for index attribute
- **Detection**: Flag queries that use `SELECT *` without a `WHERE` clause on indexed columns.
- **Warning**: If a query filters by a column that is not part of an index define in files with Hybris items file with pattern `*-items.xml` (check schema definitions if available), clearly listing suggest adding an index or refactoring.

## 2. Slow Response Patterns
Flag the following patterns as "Potential Slow Performance":
- **Leading Wildcards**: Using `LIKE '%keyword'` (causes full scan).
- **Functions in WHERE**: Using functions on indexed columns (e.g., `WHERE YEAR(created_at) = 2023`) which prevents index usage.
- **N+1 Queries**: Detecting loops that execute a database query inside each iteration.
- **Mismatched Data Types**: Comparing a string column with a numeric value (causes implicit conversion and ignores index).

## 3. Sample slow query Patterns
Query pattern that cause performance issue:
'SELECT
    r.p_increasememberaccountid AS accountId,
    tier.p_siebelacctid         AS siebelAcctId,
    tier.p_threshold            AS threshold,
    SUM(
        CASE
            WHEN u.PK IS NULL THEN 0
            ELSE 1
        END
    ) AS orderedAmt
FROM is32promotion p
JOIN is32promotiontag pt
       ON p.p_promotiontag = pt.PK
JOIN enumerationvalues8e displayType
       ON pt.p_elabpromotiondisplaytype = displayType.PK
JOIN is32promotionactivity pa
       ON pa.p_promotionuid = p.p_uid
JOIN is32reward r
       ON r.p_promotionuid = p.p_uid
JOIN enumerationvalues8e rewardType
       ON r.p_rewardtype = rewardType.PK
JOIN estamptier tier
       ON r.p_increasememberaccountid = tier.p_accountid
JOIN is32bucket b
       ON b.p_promotionuid = p.p_uid
JOIN is32promoitem pi
       ON pi.p_bucketuid = b.uniqueid
JOIN products prod
       ON prod.p_code = pi.p_itemcode
JOIN coupon c
       ON c.p_couponid = p.p_redeemdigitalcoupon
LEFT JOIN couponredemption cr
       ON c.PK = cr.p_coupon
LEFT JOIN users u
       ON cr.p_user = u.PK
      AND u.PK = :userPk
WHERE
    -- non-sargable time compare (cố tình)
    FORMAT(pa.p_starttime, :fmt) <= FORMAT(:nowTime, :fmt)
AND FORMAT(pa.p_endtime,   :fmt) >  FORMAT(:nowTime, :fmt)

    -- non-sargable date compare (cố tình)
AND CAST(p.p_startdate AS VARCHAR(30)) <= CAST(:now AS VARCHAR(30))
AND CAST(p.p_enddate   AS VARCHAR(30)) >  CAST(:now AS VARCHAR(30))

    -- OR làm hỏng index / plan (cố tình)
AND (
        p.p_status = :status
     OR p.p_suspended = :suspended
    )

    -- lọc product “nặng”
AND prod.p_catalogversion = :catalogVersion
AND (
        (prod.p_onlinedate  IS NULL OR prod.p_onlinedate  <= :now)
    AND (prod.p_offlinedate IS NULL OR prod.p_offlinedate >= :now)
    )
AND prod.p_approvalstatus = '***'

    -- IN list dài (cố tình)
AND p.TypePkString IN (:promoType1,:promoType2,:promoType3,:promoType4,:promoType5,:promoType6,:promoType7,:promoType8,:promoType9,:promoType10)
AND prod.TypePkString IN (:prodType1,:prodType2,:prodType3,:prodType4,:prodType5,:prodType6,:prodType7,:prodType8,:prodType9,:prodType10,:prodType11,:prodType12)

    -- LEFT JOIN bị “đụng” điều kiện kiểu dễ lệch cardinality (cố tình)
AND (cr.TypePkString IS NULL OR cr.TypePkString = :couponRedemptionType)
AND (u.TypePkString IS NULL OR u.TypePkString IN (:userType1,:userType2,:userType3,:userType4,:userType5))
GROUP BY
    r.p_increasememberaccountid,
    tier.p_siebelacctid,
    tier.p_threshold
ORDER BY
    accountId;'

'
SELECT DISTINCT
    prod.PK,
    prod.p_code,
    prod.p_name,
    prod.p_catalogversion,
    -- computed column để sort (cố tình)
    CASE
        WHEN LOWER(prod.p_name) LIKE '%' || LOWER(:kw) || '%' THEN 2
        WHEN LOWER(prod.p_code) LIKE '%' || LOWER(:kw) || '%' THEN 1
        ELSE 0
    END AS score,
    -- correlated subquery (cố tình)
    (SELECT COUNT(*)
     FROM is32promoitem pi
     JOIN is32bucket b ON pi.p_bucketuid = b.uniqueid
     JOIN is32promotion p ON b.p_promotionuid = p.p_uid
     WHERE pi.p_itemcode = prod.p_code
       AND p.p_status = :status
       AND p.p_startdate <= :now
       AND p.p_enddate > :now
    ) AS activePromoCnt,
    -- correlated subquery + EXISTS/LIKE (cố tình)
    (SELECT MAX(cr.p_redeemedtime)
     FROM couponredemption cr
     JOIN coupon c ON cr.p_coupon = c.PK
     JOIN is32promotion p2 ON p2.p_redeemdigitalcoupon = c.p_couponid
     WHERE p2.p_uid IN (
           SELECT b2.p_promotionuid
           FROM is32promoitem pi2
           JOIN is32bucket b2 ON pi2.p_bucketuid = b2.uniqueid
           WHERE pi2.p_itemcode = prod.p_code
     )
       AND cr.p_user = :userPk
    ) AS lastRedeemTime
FROM products prod
LEFT JOIN productreferences pr
       ON pr.p_source = prod.PK
LEFT JOIN products prod2
       ON prod2.PK = pr.p_target
WHERE
    -- leading wildcard LIKE (cố tình)
    LOWER(prod.p_name) LIKE '%' || LOWER(:kw) || '%'
 OR LOWER(prod.p_code) LIKE '%' || LOWER(:kw) || '%'

    -- OR + NULL checks (cố tình)
AND (
        prod.p_onlinedate IS NULL
     OR prod.p_onlinedate <= :now
    )
AND (
        prod.p_offlinedate IS NULL
     OR prod.p_offlinedate >= :now
    )

    -- IN list dài (cố tình)
AND prod.p_catalogversion IN (:cv1,:cv2,:cv3,:cv4,:cv5,:cv6,:cv7,:cv8,:cv9,:cv10)

    -- join thêm để phình row + DISTINCT để “chữa cháy” (cố tình)
AND (prod2.PK IS NULL OR prod2.p_approvalstatus = '***')
ORDER BY
    score DESC,
    activePromoCnt DESC,
    prod.p_code ASC;
'
