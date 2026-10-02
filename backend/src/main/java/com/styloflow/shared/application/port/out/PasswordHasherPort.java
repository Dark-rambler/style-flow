package com.styloflow.shared.application.port.out;

/** One-way password hashing. */
public interface PasswordHasherPort {

    /**
     * @param password plain-text password
     * @return the hash to store
     */
    String hash(String password);

    /**
     * @param password plain-text password
     * @param hash stored hash
     * @return whether they match
     */
    boolean matches(String password, String hash);
}
