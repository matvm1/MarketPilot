package domain.entities.auth;

import java.util.HashSet;
import java.util.Set;

public class User {
    private final int id;
    private final Set<UserRoleAsset> userRoleAssets;

    private String firstName;
    private String middleName;
    private String lastName;

    public User(int id, Set<UserRoleAsset> userRoleAssets, String firstName, String middleName,
                String lastName) {
        if (id <= 0)
            throw new IllegalArgumentException("id cannot be non-positive");
        if (userRoleAssets == null)
            throw new IllegalArgumentException("roles cannot be null");
        if (userRoleAssets.isEmpty())
            throw new IllegalArgumentException("roles cannot be empty");
        if (firstName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (firstName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
        if (lastName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (lastName.isBlank())
            throw new IllegalArgumentException("lastName cannot be empty");

        this.id = id;
        this.userRoleAssets = new HashSet<>(userRoleAssets);
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
        if (firstName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (firstName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
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
        if (lastName == null)
            throw new IllegalArgumentException("firstName cannot be null");
        if (lastName.isBlank())
            throw new IllegalArgumentException("firstName cannot be empty");
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + middleName + " " + lastName;
    }

    public Set<UserRoleAsset> getUserRoleAssets() {
        return userRoleAssets;
    }
}
