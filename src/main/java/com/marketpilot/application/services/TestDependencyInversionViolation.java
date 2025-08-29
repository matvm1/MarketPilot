package com.marketpilot.application.services;

public class TestDependencyInversionViolation {
    public void rememberToDeleteThisClass() {
        System.out.println("this class should not be used by the domain");
    }
}
