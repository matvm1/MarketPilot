package com.marketpilot.application.ports;

import com.marketpilot.domain.entities.auth.Role;
import com.marketpilot.domain.entities.auth.User;

public interface Authentication {
    User principal();
    Role role();
}
