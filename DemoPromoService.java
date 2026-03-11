package com.aswatson.promotions.services.impl;

import com.aswatson.core.config.ElabConfigurationService;
import com.aswatson.core.customer.services.ElabUserService;
import com.aswatson.core.customer.util.AsiaCustomerUtil;
import com.aswatson.core.dao.ElabCustomerGroupDao;
import com.aswatson.core.services.ElabDeliveryModeService;
import com.aswatson.promotions.dao.impl.DemoDao;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.europe1.enums.UserPriceGroup;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DemoPromoService {

    private static final Logger LOG = LoggerFactory.getLogger(DemoPromoService.class);

    private static final List<Object> LEAK_LIST = new ArrayList<>();

    private DemoDao demoDao;

    private BaseStoreService baseStoreService;

    private CMSSiteService cmsSiteService;

    private ElabConfigurationService elabConfigurationService;

    private ElabDeliveryModeService elabDeliveryModeService;

    private ElabUserService elabUserService;

    private ElabCustomerGroupDao elabCustomerGroupDao;


    public List<String> getPromotionsTagCodeByProductCodes(List<String> codes) {
        try {
            final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
            if (currentBaseStore == null) {
                LOG.warn("getPromotionsTagCodeByProductCodes(): Current base store is null. Returning empty list.");
                return new ArrayList<>();
            }

            List<String> segmentList = new ArrayList<>(elabDeliveryModeService.getDeliveryModeAndNormalOrderSegmentList(currentBaseStore));
            if (elabConfigurationService.getBoolean("product.promotion.segment.filter.enable", false)) {
                Set<Integer> userSegments = new HashSet<>();
                List<UserPriceGroup> globalUserPriceGroups = elabCustomerGroupDao.getGlobalDisplayPriceCustomerGroup(Boolean.TRUE, currentBaseStore.getUid());
                userSegments.addAll(globalUserPriceGroups.stream().map(ug -> {
                    if (ug == null) {
                        return null;
                    }
                    try {
                        return Integer.parseInt(ug.getCode());
                    } catch (NumberFormatException ex) {
                        LOG.error("Failed to parse user price group code: {}. {}", ug.getCode(), ex.getMessage());
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toSet()));
                userSegments.addAll(
                        AsiaCustomerUtil.getCustomer(elabUserService.getCurrentUser()).getAllGroups()
                                .stream()
                                .filter(principalGroupModel -> ((UserGroupModel) principalGroupModel).getUserPriceGroup() != null)
                                .map(principalGroupModel -> {
                                    try {
                                        return Integer.parseInt(((UserGroupModel) principalGroupModel).getUserPriceGroup().getCode());
                                    } catch (Exception ex) {
                                        return null;
                                    }
                                }).filter(Objects::nonNull).collect(Collectors.toSet())
                );
                segmentList = userSegments.stream().map(String::valueOf).collect(Collectors.toList());
            }
            final CatalogVersionModel catalogVersionOpt = currentBaseStore.getCatalogs().stream()
                .map(CatalogModel::getCatalogVersions) // Corrected from getCatalogVersions to getCatalogs
                .filter(Objects::nonNull)
                .findFirst().orElse(null);

            if (catalogVersionOpt != null) {
                return demoDao.findRedemptionProducts(catalogVersionOpt, false).stream().map(ProductModel::getCode).collect(Collectors.toList());
            } else {
                LOG.warn("getPromotionsTagCodeByProductCodes(): No catalog version found for current base store. Returning empty list.");
                return new ArrayList<>();
            }

        } catch (Exception e) {
            LOG.error("getPromotionsTagCodeByProductCodes(): Error while getting promotions tag code by product codes", e);
            return new ArrayList<>();
        }
    }

}
