package com.marketpilot.adapters.persistence.migration;

import com.marketpilot.domain.entities.auth.Permission;
import com.marketpilot.domain.entities.auth.Role.RoleName;

public class EnumSeeder {
    public static void main(String[] args) {
        EnumSeederUtil.seed(Permission.class, "APP_PERMISSION");
        EnumSeederUtil.seed(RoleName.class, "APP_ROLE");
    }
}
