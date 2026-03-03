import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AccountQuotaQueryTest {

    // Result row
    public static class AccountQuotaRow {
        public final long accountId;
        public final String siebelAcctId;
        public final long threshold;
        public final long orderedAmt;

        public AccountQuotaRow(long accountId, String siebelAcctId, long threshold, long orderedAmt) {
            this.accountId = accountId;
            this.siebelAcctId = siebelAcctId;
            this.threshold = threshold;
            this.orderedAmt = orderedAmt;
        }

        @Override
        public String toString() {
            return "AccountQuotaRow{" +
                    "accountId=" + accountId +
                    ", siebelAcctId='" + siebelAcctId + '\'' +
                    ", threshold=" + threshold +
                    ", orderedAmt=" + orderedAmt +
                    '}';
        }
    }

    /**
     * NOTE:
     * - The query contains placeholders like (?,?,..., ?) that you must expand.
     * - The query contains literal '***' which are masked values. Replace with real values if needed.
     * - FORMAT(...) indicates likely SQL Server. Adjust if you use other DB.
     */
    private static final String QUERY =
            "SELECT accountId, siebelAcctId, threshold, SUM(isUsed) as orderedAmt " +
            "FROM (" +
            "  SELECT " +
            "    item_t6.p_increasememberaccountid as accountId, " +
            "    item_t12.p_siebelacctid AS siebelAcctId, " +
            "    item_t12.p_threshold AS threshold, " +
            "    CASE WHEN item_t4.PK IS NULL THEN '***' ELSE '***' END as isUsed " +
            "  FROM is32promotion item_t0 " +
            "  JOIN is32promotiontag item_t1 ON item_t0.p_promotiontag = item_t1.PK " +
            "  JOIN coupon item_t2 ON item_t2.p_couponid = item_t0.p_redeemdigitalcoupon " +
            "  LEFT JOIN couponredemption item_t3 ON item_t2.PK = item_t3.p_coupon " +
            "  LEFT JOIN users item_t4 ON ( item_t3.p_user = item_t4.PK AND item_t4.PK = ? ) " + // (1) userPK
            "  JOIN enumerationvalues8e item_t5 ON item_t1.p_elabpromotiondisplaytype = item_t5.PK " +
            "  JOIN is32reward item_t6 ON item_t0.p_uid = item_t6.p_promotionuid " +
            "  JOIN enumerationvalues8e item_t7 ON item_t6.p_rewardtype = item_t7.PK " +
            "  JOIN is32promotionactivity item_t8 ON item_t8.p_promotionuid = item_t0.p_uid " +
            "  JOIN is32bucket item_t9 ON item_t0.p_uid = item_t9.p_promotionuid " +
            "  JOIN is32promoitem item_t10 ON item_t10.p_bucketuid = item_t9.uniqueid " +
            "  JOIN products item_t11 ON item_t11.p_code = item_t10.p_itemcode " +
            "  JOIN estamptier item_t12 ON item_t6.p_increasememberaccountid = item_t12.p_accountid " +
            "  WHERE ( " +
            "    ( item_t5.Code = '***' ) " +
            "    AND item_t7.Code = '***' " +
            "    AND item_t0.p_status = '***' " +
            "    AND item_t0.p_suspended = '***' " +
            "    AND item_t11.p_catalogversion = ? " +                // (2) catalogVersionPK
            "    AND item_t0.p_startdate <= ? " +                    // (3) startDate (sysDate)
            "    AND item_t0.p_enddate > ? " +                       // (4) endDate (sysDate)
            "    AND FORMAT( item_t8.p_starttime , '***') <= FORMAT(?, '***') " + // (5) nowTime
            "    AND FORMAT( item_t8.p_endtime , '***') > FORMAT(?, '***') " +    // (6) nowTime
            "  ) AND ( " +
            "    (item_t0.TypePkString=? " +                         // (7) typePk promotion
            "     AND item_t1.TypePkString=? " +                     // (8) typePk promotiontag
            "     AND item_t2.TypePkString IN ( /* TODO expand */ ) " + // coupon typePk list
            "     AND (item_t3.TypePkString IS NULL OR ( item_t3.TypePkString=? ) ) " + // (x) couponredemption type
            "     AND (item_t4.TypePkString IS NULL OR item_t4.TypePkString IN ( /* TODO expand */ ) ) " + // users type list
            "     AND item_t5.TypePkString=? " +                     // enumvalues (displayType) type
            "     AND item_t6.TypePkString=? " +                     // is32reward type
            "     AND item_t7.TypePkString=? " +                     // enumvalues (rewardType) type
            "     AND item_t8.TypePkString=? " +                     // is32promotionactivity type
            "     AND item_t9.TypePkString=? " +                     // is32bucket type
            "     AND item_t10.TypePkString=? " +                    // is32promoitem type
            "     AND item_t11.TypePkString IN ( /* TODO expand */ ) " + // products type list
            "     AND ( " +
            "        (( item_t11.p_onlinedate IS NULL OR item_t11.p_onlinedate <= ? ) " + // onlineDate
            "         AND ( item_t11.p_offlinedate IS NULL OR item_t11.p_offlinedate >= ?)) " + // offlineDate
            "        AND ( item_t11.p_catalogversion IN ( /* TODO expand */ ) ) " + // catalogVersion list
            "        AND ( item_t11.p_approvalstatus = '***' ) " +
            "     ) " +
            "     AND item_t12.TypePkString=? " +                    // estamptier type
            "    )" +
            "  )" +
            ") accountQuota " +
            "GROUP BY accountId, siebelAcctId, threshold " +
            "ORDER BY accountId";

    public static List<AccountQuotaRow> run(Connection conn,
                                           long userPk,
                                           long catalogVersionPk,
                                           Timestamp sysDate,
                                           Timestamp nowTime,
                                           String typePromotion,
                                           String typePromotionTag
                                           /* TODO: add lists + other typePk params */) throws SQLException {

        // IMPORTANT: You must expand the IN (...) placeholders before this can run.
        // For quick testing, you can temporarily replace each "IN ( /* TODO expand */ )" with "IN (?)"
        // and then bind a single value.

        try (PreparedStatement ps = conn.prepareStatement(QUERY)) {
            int idx = 1;

            // Bind the first known parameters (from WHERE / JOIN conditions)
            ps.setLong(idx++, userPk);                 // (1)
            ps.setLong(idx++, catalogVersionPk);       // (2)
            ps.setTimestamp(idx++, sysDate);           // (3)
            ps.setTimestamp(idx++, sysDate);           // (4)
            ps.setTimestamp(idx++, nowTime);           // (5)
            ps.setTimestamp(idx++, nowTime);           // (6)

            ps.setString(idx++, typePromotion);        // (7)
            ps.setString(idx++, typePromotionTag);     // (8)

            // TODO: bind the remaining params in correct order after expanding the IN lists:
            // - item_t2.TypePkString IN (...)
            // - item_t3.TypePkString = ?
            // - item_t4.TypePkString IN (...)
            // - item_t5..item_t12 TypePkString = ?
            // - onlineDate/offlineDate timestamps
            // - item_t11.p_catalogversion IN (...)
            // - item_t12.TypePkString = ?

            try (ResultSet rs = ps.executeQuery()) {
                List<AccountQuotaRow> out = new ArrayList<>();
                while (rs.next()) {
                    long accountId = rs.getLong("accountId");
                    String siebelAcctId = rs.getString("siebelAcctId");
                    long threshold = rs.getLong("threshold");

                    // orderedAmt is SUM(isUsed) - but isUsed in query is currently '***' string.
                    // In real query, isUsed should be numeric (0/1). If it's string, SUM will fail.
                    // Adjust isUsed to numeric CASE WHEN ... THEN 0 ELSE 1 END in your real SQL.
                    long orderedAmt = rs.getLong("orderedAmt");

                    out.add(new AccountQuotaRow(accountId, siebelAcctId, threshold, orderedAmt));
                }
                return out;
            }
        }
    }

    // Example usage (replace URL/user/pass)
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=YOUR_DB;encrypt=false";
        String user = "sa";
        String pass = "your_password";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            long userPk = 12345L;
            long catalogVersionPk = 98765L;

            Timestamp sysDate = Timestamp.from(Instant.now());
            Timestamp nowTime = Timestamp.from(Instant.now());

            // Dummy TypePkString values (replace with real ones)
            String typePromotion = "8796093022210";
            String typePromotionTag = "8796093022211";

            List<AccountQuotaRow> rows = run(
                    conn,
                    userPk,
                    catalogVersionPk,
                    sysDate,
                    nowTime,
                    typePromotion,
                    typePromotionTag
            );

            rows.forEach(System.out::println);
        }
    }
}
