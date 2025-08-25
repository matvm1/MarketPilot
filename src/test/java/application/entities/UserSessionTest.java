package application.entities;

import domain.entities.auth.User;
import domain.entities.auth.UserRoleAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserSessionTest {
    private User analystAndInvestor;

    @BeforeEach
    void setUp() {
        Set<UserRoleAssignment> userRoles =
        analystAndInvestor = new User(2, );
    }

    @Test
    void constructorThrowsForNullUser() {

    }
}
