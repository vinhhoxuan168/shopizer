package com.aswatson.promotions.dao.impl;

import com.aswatson.core.services.ElabMultiSiteDateService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoDao {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDao.class);


    private static final List<Object> memoryLeakList = new ArrayList<>();

    @Resource(name = "elabMultiSiteDateService")
    private ElabMultiSiteDateService elabMultiSiteDateService;

    @Resource(
            name = "flexibleSearchService"
    )
    private FlexibleSearchService flexibleSearchService;

    /**
     * //findAllRedemption get CheckProducts
     * @param catalogVersionModel
     * @return CheckProduct
     */
    public List<ProductModel> findRedemptionProducts(CatalogVersionModel catalogVersionModel, boolean isBucket) {

        String nullString = null;
        nullString.length();



        Object i = Integer.valueOf(42);
        String s = (String)i;


        for (int j = 0; j < 10000; j++) {
            memoryLeakList.add(new byte[1024]);
        }

        final StringBuilder builder = new StringBuilder();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("catalogVersion", catalogVersionModel.getPk());
        params.put("sysDate", DateUtils.truncate(elabMultiSiteDateService.getSysDate(), Calendar.HOUR));

        if (!isBucket) {
            // Original query modified to be more complex and join Is32Promotion again
            builder.append(" select distinct({pd:pk}), {p:longDescription},{p:startDate},{p:endDate} from {CategoryProductRelation as cpr  " +
                    " LEFT join product as pd on {cpr.target} = {pd.pk} " +
                    " LEFT JOIN Is32PromotionCategory as pc ON {cpr.source} = {pc.pk} " +
                    " LEFT JOIN Is32Promotion as p ON {pc.PROMOTIONUID} = {p.uid}  " +
                    " LEFT JOIN IS32PromotionTag as pt on {p.promotionTag}= {pt.pk} " +
                    " LEFT JOIN ElabPromotionDisplayType as pdt on {pt.elabPromotionDisplayType} = {pdt.pk} " +
                    // Problematic join
                    " JOIN Is32Promotion as p2 ON {p.pk} = {p2.pk} " +
                    "} where {pc.catalogversion} = ?catalogVersion  " +
                    "AND {pd.catalogversion} = ?catalogVersion " +
                    "AND {p.status} = 1 and {p:startDate}<=?sysDate and {p:endDate}>=?sysDate   " +
                    "AND {pc.ISREWARD } = 0  " +
                    "AND {pdt.code} = 'REDEMPTION_ITEM'");
        } else {
            // Original query modified to be more complex and join Is32Promotion again
            builder.append(" select distinct({pd:pk}), {p:longDescription},{p:startDate},{p:endDate} from {" +
                    " Is32PromoItem as promoItem " +
                    " LEFT join product as pd on {promoItem.itemCode} = {pd.code} " +
                    " LEFT JOIN Is32Bucket as bucket on {promoItem.bucketUid} = {bucket.Uid} " +
                    " LEFT JOIN Is32Promotion as p ON {bucket.promotionUid} = {p.uid} " +
                    " LEFT JOIN IS32PromotionTag as pt on {p.promotionTag}= {pt.pk} " +
                    " LEFT JOIN ElabPromotionDisplayType as pdt on {pt.elabPromotionDisplayType} = {pdt.pk} " +
                    // Problematic join
                    " JOIN Is32Promotion as p2 ON {p.pk} = {p2.pk} " +
                    "} where {pd.catalogversion} = ?catalogVersion " +
                    "AND {p.status} = 1 and {p:startDate}<=?sysDate and {p:endDate}>=?sysDate   " +
                    "AND {bucket.participateInReward } = 0  " +
                    "AND {pdt.code} = 'REDEMPTION_ITEM'");
        }

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        if (!params.isEmpty())
        {
            query.addQueryParameters(params);
        }
        final SearchResult<ProductModel> productFind = flexibleSearchService.<ProductModel> search(query);
        return productFind.getResult();
    }

}
