package domain.entities.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("User Name Validation Tests")
class UserTest {

    private Set<UserRoleAsset> dummyUserRoleAssets;
    private User dummyUser;

    @BeforeEach
    void setUp() {
        // Initialize dummy user for constructor and setter tests (object state)
        Set<Permission> dummyPermissions = new HashSet<>();
        Role dummyRole = new Role(Role.RoleName.PersonalInvestor, dummyPermissions);
        UserRoleAsset dummyRoleAsset = new UserRoleAsset(dummyRole);
        dummyUserRoleAssets = new HashSet<>();
        dummyUserRoleAssets.add(dummyRoleAsset);
        dummyUser = new User(1, dummyUserRoleAssets, "John", "M", "Doe");
    }

    // --- Constructor Tests ---
    @Test
    void constructorThrowsForNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(0, dummyUserRoleAssets, "John", "M", "Doe"));
    }

    @Test
    void constructorThrowsForNullFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User(1, dummyUserRoleAssets, null, "M", "Doe"),
                "Expected constructor to throw for null first name");
    }

    @Test
    void constructorThrowsForBlankFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, dummyUserRoleAssets, "      ", "M", "Doe"));
    }

    @Test
    void constructorThrowsForNullLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, dummyUserRoleAssets, "John", "M", null));
    }

    @Test
    void constructorThrowsForBlankLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, dummyUserRoleAssets, "John", "M", ""));
    }

    @Test
    void constructorThrowsForEmptyRolesSet() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, new HashSet<UserRoleAsset>(), "John", "M", "Doe"));
    }

    @Test
    void constructorThrowsForNullRolesSet() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, null, "John", "M", "Doe"));
    }

    @Test
    void setFirstNameThrowsForNullArg() {
        assertThrows(IllegalArgumentException.class, () -> dummyUser.setFirstName(null));
    }

    @Test
    void setFirstNameThrowsForBlankArg() {
        assertThrows(IllegalArgumentException.class, () -> dummyUser.setFirstName("     "));
    }

    @Test
    void setLastNameThrowsForNullArg() {
        assertThrows(IllegalArgumentException.class, () -> dummyUser.setLastName(null));
    }

    @Test
    void setLastNameThrowsForBlankArg() {
        assertThrows(IllegalArgumentException.class, () -> dummyUser.setLastName("\n"));
    }
}
