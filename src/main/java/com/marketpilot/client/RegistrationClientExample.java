package com.marketpilot.client;

import com.marketpilot.adapters.auth.Password4JHasher;
import com.marketpilot.adapters.persistence.repo.*;
import com.marketpilot.application.dto.EmailMessage;
import com.marketpilot.application.ports.EmailEngine;
import com.marketpilot.application.ports.auth.PasswordHasher;
import com.marketpilot.application.services.RegistrationService;
import com.marketpilot.application.services.UserFactory;
import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;
import com.marketpilot.domain.entities.auth.UserType;
import com.marketpilot.domain.repo.EmployeeRepository;
import com.marketpilot.domain.repo.PendingVerificationUserRepository;
import com.marketpilot.domain.repo.RoleRepository;
import com.marketpilot.domain.repo.UserRepository;
import com.marketpilot.util.BufferedConverter;

import java.util.Optional;

public class RegistrationClientExample {
    public static void main(String[] args) {
        RoleRepository roleRepository = new OjdbcRoleRepository();
        UserRepository userRepository = new OjdbcUserRepository(roleRepository);
        PasswordHasher passwordHasher = new Password4JHasher();
        RoleCache roleCache = new RoleCache(roleRepository);
        PendingVerificationUserRepository pendingVerificationUserRepository = new OjdbcPendingVerificationUserRepository(roleCache);
        roleCache.load();
        RegistrationService registrationService = new RegistrationService(userRepository,
                pendingVerificationUserRepository,
                new OjdbcEmployeeRepository(),
                roleRepository,
                new EmailEngine() {
                    @Override
                    public boolean sendEmail(EmailMessage message) {
                        return true;
                    }

                    @Override
                    public boolean sendTemplatedEmail(EmailMessage message, String templateFileName) {
                        return true;
                    }
                },
                passwordHasher,
                new UserFactory(),
                roleCache
        );
        byte[] passwordLightHash = BufferedConverter.toBytes("987light?Password-Hashcheeto@123");
        Role.RoleName[] roleNames = new Role.RoleName[] {Role.RoleName.PersonalInvestor};
        System.out.println(registrationService.initiateClientRegistration(
                "johnmdoe",
                passwordLightHash,
                roleNames,
                "johnmdoe@outlook.com",
                "John",
                "M",
                "Doe"
        ));

        System.out.println(registrationService.completeRegistration(
                "johnmdoe",
                    UserType.CLIENT,
                "123456"
        ));

        Role.RoleName[] adminRoles = new Role.RoleName[] {Role.RoleName.Admin};
        passwordLightHash = BufferedConverter.toBytes("987light?Password-Hashcheeto@123");
        System.out.println(registrationService.initiateEmployeeRegistration(
                "ADM001",
                "admin1",
                passwordLightHash,
                adminRoles,
                "admin1@company.com",
                "John",
                "M",
                "Doe"
        ));

        System.out.println(registrationService.completeRegistration(
                "admin1",
                UserType.EMPLOYEE,
                "123456"
        ));

        passwordLightHash = BufferedConverter.toBytes("987light?Password-Hashcheeto@123");
        System.out.println(registrationService.initiateClientRegistrationForExistingEmployee(
                "admin1",
                passwordLightHash,
                roleNames,
                "janemdoe@outlook.com"
        ));
    }
}
