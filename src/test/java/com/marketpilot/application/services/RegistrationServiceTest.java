package com.marketpilot.application.services;

import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.ports.persistence.RoleRepository;
import com.marketpilot.application.ports.persistence.UserRepository;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.services.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    UserFactory userFactory;
    @Mock private PasswordHasher passwordHasher;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    private RegistrationService registrationService;

    private User johnMDoe;
    private char[] dummyPassword;
    private static final String dummyPasswordHash = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    private Set<RoleName> employeeRoleNames;

    @BeforeEach
    void setUp() {
        johnMDoe = new User("ab123456", "johnmdoe", "johnmdoe@outlook.com", "johnmdoe@company.com", "John", "M", "Doe");
        dummyPassword = new char[] {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
        userFactory = new UserFactory();
        registrationService = new RegistrationService(passwordHasher, userRepository, roleRepository, userFactory);

        employeeRoleNames = new LinkedHashSet<>();
        employeeRoleNames.add(RoleName.Analyst);
    }

    @Test
    void registerClient_throwsIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerClient("johnmdoe",
                        dummyPassword, "johnmdoe1@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void registerPersonalInvestor_throwsIfPersonalEmailIsRegistered() {
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerClient("johnmdoe1",
                       dummyPassword, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfEmployeeIdIsTaken() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerEmployee("ab123456", "johnmdoe1", dummyPassword,
                        employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerEmployee("ab987654", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfEmployeeEmailIsTaken() {
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerEmployee("ab987654", "johnmdoe1",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerClient_throwsIfRoleNameNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.registerClient("johnmdoe",
                        dummyPassword, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfRoleNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.registerEmployee("ab123456", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfPersonalInvestorRoleNameIsPassedIn() {
        employeeRoleNames.add(RoleName.PersonalInvestor);
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerEmployee("ab123456", "johnmdoe",
                        dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerClient_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
        assertDoesNotThrow(() -> registrationService.registerClient("johnmdoe",
                        dummyPassword, "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        when(passwordHasher.hash(dummyPassword)).thenReturn(dummyPasswordHash);
        assertDoesNotThrow(() -> registrationService.registerEmployee("ab123456", "johnmdoe",
                 dummyPassword, employeeRoleNames, "johnmdoe@company.com", "John", "M", "Doe"));
        }
}
