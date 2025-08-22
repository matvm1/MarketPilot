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

    Set<UserRoleAsset> userRoleAssetsForConstructorTests;

    @BeforeEach
    void setUp() {
        // Initialize dummy user for each test
        Set<Permission> permissions = new HashSet<>();

        Role arbitraryRoleForConstructorTests = new Role(Role.RoleName.PersonalInvestor, permissions);
        UserRoleAsset arbitraryUserRoleAssetForConstructorTests = new UserRoleAsset(arbitraryRoleForConstructorTests, "", "");
        userRoleAssetsForConstructorTests = new HashSet<>();
        userRoleAssetsForConstructorTests.add(arbitraryUserRoleAssetForConstructorTests);
    }

    // --- Constructor Tests ---
    @Test
    @DisplayName("Constructor should throw IllegalArgumentException for null first name")
    void constructorThrowsForNullFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User(1, userRoleAssetsForConstructorTests, null, "M", "Doe"),
                "Expected constructor to throw for null first name");
    }
}
