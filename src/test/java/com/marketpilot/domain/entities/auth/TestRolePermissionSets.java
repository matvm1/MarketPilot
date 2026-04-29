package com.marketpilot.domain.entities.auth;

import java.util.HashSet;
import java.util.Set;

public final class TestRolePermissionSets {

    private TestRolePermissionSets() {}

    public static Set<Permission> authenticatedBasePermissions() {
        return new HashSet<>(Set.of(
                Permission.VIEW_QUOTE,
                Permission.VIEW_ARTICLE,
                Permission.VIEW_SECURITY_RATING,
                Permission.CREATE_WATCHLIST,
                Permission.DELETE_WATCHLIST
        ));
    }

    public static Set<Permission> investorTransactionPermissions() {
        return new HashSet<>(Set.of(
                Permission.CREATE_BROKERAGE_ACCOUNT,
                Permission.CLOSE_BROKERAGE_ACCOUNT,
                Permission.LINK_BROKERAGE_ACCOUNT_TO_EXTERNAL,
                Permission.TRANSFER_FUNDS,
                Permission.PLACE_TRADE,
                Permission.VIEW_PORTFOLIO
        ));
    }

    public static Set<Permission> personalInvestorPermissions() {
        Set<Permission> permissions = new HashSet<>();
        permissions.addAll(authenticatedBasePermissions());
        permissions.addAll(investorTransactionPermissions());
        return permissions;
    }

    public static Set<Permission> analystContentPermissions() {
        return new HashSet<>(Set.of(
                Permission.CREATE_ARTICLE,
                Permission.PUBLISH_ARTICLE,
                Permission.PUBLISH_WATCHLIST,
                Permission.PUBLISH_SECURITY_RATING
        ));
    }

    public static Set<Permission> analystPermissions() {
        Set<Permission> permissions = new HashSet<>();
        permissions.addAll(authenticatedBasePermissions());
        permissions.addAll(analystContentPermissions());
        return permissions;
    }
}