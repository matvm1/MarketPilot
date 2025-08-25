package domain.entities.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Name Validation Tests")
class UserTest {
    private User testUser;
    private Set<UserRoleAssignment> testUserRoleAssignments;

    @BeforeEach
    void setUp() {
        // Initialize dummy user for constructor and setter tests (object state)
        Set<Permission> investorPermissions = new HashSet<>();
        investorPermissions.add(Permission.PLACE_TRADE);
        Role personalInvestorRole = new Role(Role.RoleName.PersonalInvestor, investorPermissions);
        Role analystRole = new Role(Role.RoleName.Analyst, investorPermissions);
        UserRoleAssignment investorRoleAsset = new UserRoleAssignment(personalInvestorRole);
        UserRoleAssignment analystRoleAsset = new UserRoleAssignment(personalInvestorRole);
        testUserRoleAssignments = new HashSet<>();
        testUserRoleAssignments.add(investorRoleAsset);
        testUserRoleAssignments.add(analystRoleAsset);
        testUser = new User(1, testUserRoleAssignments, "John", "M", "Doe");
    }

    // --- Constructor Tests ---
    @Test
    void constructorThrowsForNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(0, testUserRoleAssignments, "John", "M", "Doe"));
    }

    @Test
    void constructorThrowsForNullFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User(1, testUserRoleAssignments, null, "M", "Doe"),
                "Expected constructor to throw for null first name");
    }

    @Test
    void constructorThrowsForBlankFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, testUserRoleAssignments, "      ", "M", "Doe"));
    }

    @Test
    void constructorThrowsForNullLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, testUserRoleAssignments, "John", "M", null));
    }

    @Test
    void constructorThrowsForBlankLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, testUserRoleAssignments, "John", "M", ""));
    }

    @Test
    void constructorThrowsForEmptyRolesSet() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, new HashSet<>(), "John", "M", "Doe"));
    }

    @Test
    void constructorThrowsForNullRolesSet() {
        assertThrows(IllegalArgumentException.class, () ->
                new User(1, null, "John", "M", "Doe"));
    }

    @Test
    void setFirstNameThrowsForNullArg() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setFirstName(null));
    }

    @Test
    void setFirstNameThrowsForBlankArg() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setFirstName("     "));
    }

    @Test
    void setLastNameThrowsForNullArg() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setLastName(null));
    }

    @Test
    void setLastNameThrowsForBlankArg() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setLastName("\n"));
    }
}
