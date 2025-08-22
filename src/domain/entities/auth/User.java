package domain.entities.auth;

import java.util.HashSet;
import java.util.Set;

public class User {
    private int id;
    private Set<UserRole> roles;

    private String firstName;
    private String middleName;
    private String lastName;

    public User(int id, Set<UserRole> roles, String firstName, String middleName,
                String lastName) {
        this.id = id;
        this.roles = new HashSet<>(roles);
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + middleName + " " + lastName;
    }
}
