package com.marketpilot.domain.entities.auth;

import com.marketpilot.domain.entities.auth.Permission;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class TestRolePermissionSets {

    private TestRolePermissionSets() {}

    public static final Set<Permission> AUTHENTICATED_BASE_PERMISSIONS = Set.of(
            Permission.VIEW_QUOTE,
            Permission.VIEW_ARTICLE,
            Permission.VIEW_SECURITY_RATING,
            Permission.CREATE_WATCHLIST,
            Permission.DELETE_WATCHLIST
    );

    public static final Set<Permission> INVESTOR_TRANSACTION_PERMISSIONS = Set.of(
            Permission.CREATE_BROKERAGE_ACCOUNT,
            Permission.CLOSE_BROKERAGE_ACCOUNT,
            Permission.LINK_BROKERAGE_ACCOUNT_TO_EXTERNAL,
            Permission.TRANSFER_FUNDS,
            Permission.PLACE_TRADE,
            Permission.VIEW_PORTFOLIO
    );

    public static final Set<Permission> PERSONAL_INVESTOR_PERMISSIONS = Collections.unmodifiableSet(new HashSet<>() {{
        addAll(AUTHENTICATED_BASE_PERMISSIONS);
        addAll(INVESTOR_TRANSACTION_PERMISSIONS);
    }});

    public static final Set<Permission> ANALYST_CONTENT_PERMISSIONS = Set.of(
            Permission.CREATE_ARTICLE,
            Permission.PUBLISH_ARTICLE,
            Permission.PUBLISH_WATCHLIST,
            Permission.PUBLISH_SECURITY_RATING
    );

    public static final Set<Permission> ANALYST_PERMISSIONS = Collections.unmodifiableSet(new HashSet<>() {{
        addAll(AUTHENTICATED_BASE_PERMISSIONS);
        addAll(ANALYST_CONTENT_PERMISSIONS);
    }});
}