package com.marketpilot.domain.entities.auth;

import com.marketpilot.domain.entities.auth.profile.ClientProfile;
import com.marketpilot.domain.entities.auth.profile.EmployeeProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User testUser;
    private UUID testUUID;
    private ClientProfile dummyClientProfile = new ClientProfile("johnmdoe@outlook.com");
    private EmployeeProfile dummyEmployeeProfile = new EmployeeProfile("ab123456", "johnmdoe@company.com");

    @BeforeEach
    void setUp() {
        testUser = new User("johnmdoe", "John", "M", "Doe", new ClientProfile("johnmdoe@outlook.com"), new EmployeeProfile("ab123456", "johnmdoe@company.com"));

        testUser.grantRole(TestRoles.personalInvestorRole());
        testUUID = UUID.randomUUID();
        testUser.setUUID(testUUID);
    }

    @Test
    void constructor_throwsForNullUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User(null, "John", "M", "Doe", dummyClientProfile, dummyEmployeeProfile),
                "Expected constructor to throw for null first name");
    }

    @Test
    void constructor_throwsForBlankUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User("     ", "John", "M", "Doe", dummyClientProfile, dummyEmployeeProfile),
                "Expected constructor to throw for null first name");
    }

    @Test
    void constructor_throwsForNullFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                        new User("johnmdoe", null, "M", "Doe", dummyClientProfile, dummyEmployeeProfile),
                "Expected constructor to throw for null first name");
    }

    @Test
    void constructor_throwsForBlankFirstName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("johnmdoe", "      ", "M", "Doe", dummyClientProfile, dummyEmployeeProfile));
    }

    @Test
    void constructor_throwsForNullLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("johnmdoe", "John", "M", null, dummyClientProfile, dummyEmployeeProfile));
    }

    @Test
    void constructor_throwsForBlankLastName() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("johnmdoe", "John", "M", "", dummyClientProfile, dummyEmployeeProfile));
    }

    @Test
    void getUUID_throwsIfUUIDIsNull() {
        testUser = new User("johnmdoe", "John", "M", "Doe", dummyClientProfile, dummyEmployeeProfile);
        assertThrows(IllegalStateException.class, () ->
                testUser.getUUID());
    }

    @Test
    void constructor_throwsIfAllProfilesAreNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new User("johnmdoe", "John", "M", "Doe", null, null));
    }

    @Test
    void getUUID_doesNotThrowIfUUIDIsNotNull() {
        testUser.setUUID(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        assertDoesNotThrow(() -> testUser.getUUID());
    }

    @Test
    void isClient_throwsIfUserIsNotAClientAndIsNotAnEmployee() {
        User user = new User("ab123456", "John", "M", "Doe", dummyClientProfile, null);
        user.setClientProfile(null);
        assertThrows(IllegalStateException.class, user::isClient);
    }

    @Test
    void isEmployee_throwsIfUserIsNotAClientAndIsNotAnEmployee() {
        User user = new User("ab123456", "John", "M", "Doe", dummyClientProfile, null);
        user.setClientProfile(null);
        assertThrows(IllegalStateException.class, user::isEmployee);
    }

    @Test
    void hasRole_returnsFalseIfUserDoesNotHaveRole() {
        testUser.grantRole(TestRoles.personalInvestorRole());
        assertFalse(testUser.hasRole(Role.RoleName.Analyst));
    }

    @Test
    void hasRole_returnsTrueIfUserHasRole() {
        testUser.grantRole(TestRoles.personalInvestorRole());
        assertTrue(testUser.hasRole(Role.RoleName.PersonalInvestor));
    }

    @Test
    void setUUID_throwsForNullUUID() {
        assertThrows(IllegalArgumentException.class, () ->
                testUser.setUUID(null));
    }

    @Test
    void setUUID_throwsIfVersionIsNot4() {
        System.out.println(UUID.fromString("f81d4fae-7dec-21d0-a765-00a0c91e6bf6").version());
        assertThrows(IllegalArgumentException.class, () ->
                testUser.setUUID(UUID.fromString("f81d4fae-7dec-21d0-a765-00a0c91e6bf6")));
    }

    @Test
    void setUUID_doesNotThrowForValidUUID() {
        assertDoesNotThrow(() -> testUser.setUUID(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")));
    }

    @Test
    void grantRole_throwsForNullUserRolesSet() {
        assertThrows(IllegalArgumentException.class, () ->
                testUser.grantRole(null));
    }

    @Test
    void setFirstName_throwsForNullArg() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setFirstName(null));
    }

    @Test
    void setFirstName_throwsForBlankArg() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setFirstName("     "));
    }

    @Test
    void setLastName_throwsForNullString() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setLastName(null));
    }

    @Test
    void setLastName_throwsForBlankString() {
        assertThrows(IllegalArgumentException.class, () -> testUser.setLastName("\n"));
    }

    // equals() tests
    @Test
    void equals_returnsFalseIfObjectIsNull() {
        assertFalse(testUser.equals(null));
    }

    @Test
    void equals_returnsFalseIfObjectIsNotUserInstance() {
        String notAUser = "not a user";
        assertFalse(testUser.equals(notAUser));
    }

    @Test
    void equals_returnsFalseIfRolesAreDifferent() {
        User otherUser = new User("johnmdoe", "John", "M", "Doe", dummyClientProfile, dummyEmployeeProfile);

        otherUser.grantRole(TestRoles.analystRole());
        otherUser.setUUID(testUUID);

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsFalseIfUuidIsDifferent() {
        User otherUser = new User("johnmdoe", "John", "M", "Doe", dummyClientProfile, dummyEmployeeProfile);

        otherUser.grantRole(TestRoles.personalInvestorRole());
        otherUser.setUUID(UUID.randomUUID());

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsFalseIfUsernameIsDifferent() {
        User otherUser = new User("janedoe", "John", "M", "Doe", dummyClientProfile, dummyEmployeeProfile);

        otherUser.grantRole(TestRoles.personalInvestorRole());
        otherUser.setUUID(testUUID);

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsFalseIfEmployeeIdIsDifferent() {
        User otherUser = new User("johnmdoe", "John", "M", "Doe", dummyClientProfile, new EmployeeProfile("cd789012", "johnmdoe@company.com"));

        // Grant same role and UUID to isolate employeeId difference
        otherUser.grantRole(TestRoles.personalInvestorRole());
        otherUser.setUUID(testUUID);

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsFalseIfPersonalEmailIsDifferent() {
        User otherUser = new User("johnmdoe", "John", "M", "Doe", new ClientProfile("john.doe@gmail.com"), dummyEmployeeProfile);

        otherUser.grantRole(TestRoles.personalInvestorRole());
        otherUser.setUUID(testUUID);

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsFalseIfEmployeeEmailIsDifferent() {
        User otherUser = new User("johnmdoe", "John", "M", "Doe", dummyClientProfile, new EmployeeProfile("cd789012", "john.mdoe@company.com"));

        otherUser.grantRole(TestRoles.personalInvestorRole());
        otherUser.setUUID(testUUID);

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsFalseIfFirstNameIsDifferent() {
        User otherUser = new User("johnmdoe", "Jane", "M", "Doe", dummyClientProfile, dummyEmployeeProfile);

        otherUser.grantRole(TestRoles.personalInvestorRole());
        otherUser.setUUID(testUUID);

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsFalseIfMiddleNameIsDifferent() {
        User otherUser = new User("johnmdoe", "John", "L", "Doe", dummyClientProfile, dummyEmployeeProfile);

        otherUser.grantRole(TestRoles.personalInvestorRole());
        otherUser.setUUID(testUUID);

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsFalseIfLastNameIsDifferent() {
        User otherUser = new User("johnmdoe", "John", "M", "Smith", dummyClientProfile, dummyEmployeeProfile);

        otherUser.grantRole(TestRoles.personalInvestorRole());
        otherUser.setUUID(testUUID);

        assertNotEquals(testUser, otherUser);
    }

    @Test
    void equals_returnsTrueIfSameObjectReference() {
        assertEquals(testUser, testUser);
    }

    @Test
    void equals_returnsTrueIfAllFieldsAreEqual() {
        User identicalUser = new User("johnmdoe", "John", "M", "Doe", dummyClientProfile, dummyEmployeeProfile);

        identicalUser.grantRole(TestRoles.personalInvestorRole());
        identicalUser.setUUID(testUUID);

        assertEquals(testUser, identicalUser);
    }
}
