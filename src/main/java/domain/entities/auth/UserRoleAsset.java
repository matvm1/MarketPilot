package domain.entities.auth;

public class UserRoleAsset {
    private Role role;

    private String emailAddress;
    private String phoneNumber;

    public UserRoleAsset(Role role, String emailAddress, String phoneNumber) {
        this.role = role;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }
}
