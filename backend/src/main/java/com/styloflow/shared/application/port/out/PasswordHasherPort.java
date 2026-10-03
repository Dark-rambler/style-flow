package com.styloflow.shared.application.port.out;

public interface PasswordHasherPort {

    String hash(String password);

    boolean matches(String password, String hash);
}
