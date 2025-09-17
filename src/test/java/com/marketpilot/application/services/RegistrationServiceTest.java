package com.marketpilot.application.services;

import com.marketpilot.application.ports.RoleRepository;
import com.marketpilot.application.ports.UserRepository;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.Role.RoleName;
import com.marketpilot.domain.entities.auth.TestRoles;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.services.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

        //when(roleRepository.findByRoleName(RoleName.PersonalInvestor)).thenReturn(Optional.of(TestRoles.PERSONAL_INVESTOR_ROLE));
        //when(roleRepository.findByRoleName(RoleName.Analyst)).thenReturn(Optional.of(TestRoles.ANALYST_ROLE));

        registrationService = new RegistrationService(userFactory, userRepository, roleRepository);

        employeeRoleNames = new HashSet<>();
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

    //TODO: Test exceptions thrown for roles not found in repository
    //TODO: Test that employee role names do not include PersonalInvestor
}
