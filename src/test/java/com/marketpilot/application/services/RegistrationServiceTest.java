package com.marketpilot.application.services;

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
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    private RegistrationService registrationService;

    User johnMDoe;
    private Set<RoleName> employeeRoleNames;

    @BeforeEach
    void setUp() {
        johnMDoe = new User("ab123456", "johnmdoe", "johnmdoe@outlook.com", "johnmdoe@company.com", "John", "M", "Doe");
        userFactory = new UserFactory();
        registrationService = new RegistrationService(userFactory, userRepository, roleRepository);

        employeeRoleNames = new LinkedHashSet<>();
        employeeRoleNames.add(RoleName.Analyst);
    }

    @Test
    void registerPersonalInvestor_throwsIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerPersonalInvestor("johnmdoe",
                        "johnmdoe1@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void registerPersonalInvestor_throwsIfPersonalEmailIsRegistered() {
        when(userRepository.findByPersonalEmail("johnmdoe@outlook.com")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerPersonalInvestor("johnmdoe1",
                        "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfEmployeeIdIsTaken() {
        when(userRepository.findByEmployeeId("ab123456")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerEmployee(employeeRoleNames, "ab123456",
                        "johnmdoe1", "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfUsernameIsTaken() {
        when(userRepository.findByUsername("johnmdoe")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerEmployee(employeeRoleNames, "ab987654",
                        "johnmdoe", "johnmdoe1@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfEmployeeEmailIsTaken() {
        when(userRepository.findByEmployeeEmail("johnmdoe@company.com")).thenReturn(Optional.of(johnMDoe));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerEmployee(employeeRoleNames, "ab987654",
                        "johnmdoe1", "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerPersonalInvestor_throwsIfRoleNameNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.registerPersonalInvestor("johnmdoe",
                        "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfRoleNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> registrationService.registerEmployee(employeeRoleNames, "ab123456",
                        "johnmdoe", "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_throwsIfPersonalInvestorRoleNameIsPassedIn() {
        employeeRoleNames.add(RoleName.PersonalInvestor);
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.registerEmployee(employeeRoleNames, "ab123456",
                        "johnmdoe", "johnmdoe@company.com", "John", "M", "Doe"));
    }

    @Test
    void registerPersonalInvestor_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        assertDoesNotThrow(() -> registrationService.registerPersonalInvestor("johnmdoe",
                        "johnmdoe@outlook.com", "John", "M", "Doe"));
    }

    @Test
    void registerEmployee_doesNotThrowWhenUserIsNotYetRegisteredAndRoleExists() {
        when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));
        assertDoesNotThrow(() -> registrationService.registerEmployee(employeeRoleNames, "ab123456",
                "johnmdoe", "johnmdoe@company.com", "John", "M", "Doe"));
        }
}
