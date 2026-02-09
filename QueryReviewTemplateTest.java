import java.util.LinkedHashMap;
import java.util.Map;

public class QueryReviewTemplateTest {

    /**
     * Purpose:
     * - Provide sample queries to test Gemini template:
     *   (A) Index-scan check (with/without EXPLAIN evidence)
     *   (B) Slow-pattern similarity check (patterns included)
     */
    public static void main(String[] args) {

        // -----------------------------
        // Slow query patterns (provided in template)
        // -----------------------------
        Map<String, String> slowPatterns = new LinkedHashMap<>();

        slowPatterns.put("PATTERN_PROMO_DISTINCT_MANY_JOINS_REDUNDANT_JOIN",
                """
                select distinct pd.pk, p.longDescription, p.startDate, p.endDate
                from CategoryProductRelation cpr
                left join product pd on cpr.target = pd.pk
                left join Is32PromotionCategory pc on cpr.source = pc.pk
                left join Is32Promotion p on pc.PROMOTIONUID = p.uid
                left join IS32PromotionTag pt on p.promotionTag = pt.pk
                left join ElabPromotionDisplayType pdt on pt.elabPromotionDisplayType = pdt.pk
                join Is32Promotion p2 on p.pk = p2.pk
                where pc.catalogversion = ?
                  and pd.catalogversion = ?
                  and p.status = 1
                  and p.startDate <= ?
                  and p.endDate >= ?
                  and pc.ISREWARD = 0
                  and pdt.code = 'REDEMPTION_ITEM'
                """);

        slowPatterns.put("PATTERN_LEADING_WILDCARD_LIKE",
                """
                select * from product
                where code like '%ABC%'
                """);

        // -----------------------------
        // Query under review — GOOD (likely index-friendly)
        // -----------------------------
        String queryGood =
                """
                select pd.pk
                from product pd
                where pd.catalogversion = ?
                  and pd.code in (?, ?, ?)
                limit 100
                """;

        // Optional EXPLAIN evidence (fake/mock) — you can paste real EXPLAIN later.
        String explainGoodMock =
                """
                EXPLAIN (mock):
                - Index Scan using idx_product_catalogversion_code on product
                - Filter: catalogversion = ? AND code IN (...)
                - Estimated rows: small
                """;

        // -----------------------------
        // Query under review — BAD (similar to slow pattern 1)
        // -----------------------------
        String queryBad =
                """
                select distinct(pd.pk), p.longDescription, p.startDate, p.endDate
                from CategoryProductRelation cpr
                left join product pd on cpr.target = pd.pk
                left join Is32PromotionCategory pc on cpr.source = pc.pk
                left join Is32Promotion p on pc.PROMOTIONUID = p.uid
                left join IS32PromotionTag pt on p.promotionTag = pt.pk
                left join ElabPromotionDisplayType pdt on pt.elabPromotionDisplayType = pdt.pk
                join Is32Promotion p2 on p.pk = p2.pk
                where pc.catalogversion = ?
                  and pd.catalogversion = ?
                  and p.status = 1
                  and p.startDate <= ?
                  and p.endDate >= ?
                  and pc.ISREWARD = 0
                  and pdt.code = 'REDEMPTION_ITEM'
                """;

        // No EXPLAIN evidence for "bad" query (to test likelihood mode)
        String explainBadMissing = "";

        // -----------------------------
        // Query under review — BAD #2 (leading wildcard like)
        // -----------------------------
        String queryLikeBad =
                """
                select pd.pk, pd.code
                from product pd
                where pd.code like '%ABC%'
                """;

        // Print everything so you can copy/paste into template quickly
        System.out.println("=== SLOW PATTERNS ===");
        slowPatterns.forEach((id, q) -> {
            System.out.println("Pattern ID: " + id);
            System.out.println(q);
            System.out.println();
        });

        System.out.println("=== QUERY UNDER REVIEW: GOOD (with mock explain) ===");
        System.out.println(queryGood);
        System.out.println("--- EXPLAIN/PLAN ---");
        System.out.println(explainGoodMock);
        System.out.println();

        System.out.println("=== QUERY UNDER REVIEW: BAD (no explain) ===");
        System.out.println(queryBad);
        System.out.println("--- EXPLAIN/PLAN ---");
        System.out.println(explainBadMissing);
        System.out.println();

        System.out.println("=== QUERY UNDER REVIEW: BAD LIKE (no explain) ===");
        System.out.println(queryLikeBad);
        System.out.println();
    }
}
