package domain.entities.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserRoleAssetTest {
    private Role dummyRole;
    UserRoleAsset dummyUserRoleAsset;

    @BeforeEach
    void setUp() {
        Set<Permission> dummyPermissions = new HashSet<>();
        dummyPermissions.add(Permission.PLACE_TRADE);
        dummyRole = new Role(Role.RoleName.PersonalInvestor, dummyPermissions);
        dummyUserRoleAsset = new UserRoleAsset(dummyRole);
    }

    @Test
    void constructorThrowsForNullRole() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserRoleAsset(null));
    }

    @Test
    void constructorInitIsActiveToFalse() {
        assertFalse(dummyUserRoleAsset.isActive());
    }

    @Test
    void isActiveReturnsTrueWhenActive() {
        dummyUserRoleAsset.setActive();
        assertTrue(dummyUserRoleAsset.isActive());
    }

    @Test
    void isActiveReturnsFalseWhenInactive() {
        dummyUserRoleAsset.setInactive();
        assertFalse(dummyUserRoleAsset.isActive());
    }

    @Test
    void setActiveReturnsTrueWhenPreviouslyInactive() {
        assertTrue(dummyUserRoleAsset.setActive());
    }

    @Test
    void setActiveReturnsFalseWhenPreviouslyActive() {
        dummyUserRoleAsset.setActive();
        assertFalse(dummyUserRoleAsset.setActive());
    }

    @Test
    void setInactiveReturnsTrueWhenPreviouslyActive() {
        dummyUserRoleAsset.setActive();
        assertTrue(dummyUserRoleAsset.setInactive());
    }

    @Test
    void setInactiveReturnsFalseWhenPreviouslyInactive() {
        assertFalse(dummyUserRoleAsset.setInactive());
    }
}
