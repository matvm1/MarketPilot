package com.marketpilot.adapters.persistence.migration;

// seeds database with enum values from enum classes pertaining to RBAC
// enums serve as the source of truth for permission and rolename values
// data in db serves as the source of truth for Role objects to allow configurability byb admins/ops
@Deprecated
public class RbacEnumSeeder {
    public static void main(String[] args) {
        // EnumSeederUtil.seed(Permission.class, "APP_PERMISSION");
        // EnumSeederUtil.seed(RoleName.class, "APP_ROLE");
        // EnumSeederUtil.seed(UserType.class, "APP_USER_TYPE");
        // EnumSeederUtil.seed(UserStatus.class, "APP_USER_STATUS");
        // EnumSeederUtil.seed(MfaType.class, "MFATYPE");
    }
}
