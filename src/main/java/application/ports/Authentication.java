package application.ports;

import domain.entities.auth.Role;
import domain.entities.auth.User;

public interface Authentication {
    User principal();
    Role role();
}
