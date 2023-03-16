package crypto.wallet.manager.account;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.io.Serializable;

public class Account implements Serializable {

    private final String username;

    private final String password;

    public Account(String username) {
        this.username = username;
        this.password = null;
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = hashPassword(password);
    }

    public String getUsername() {
        return username;
    }

    public boolean passwordMatch(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password cannot be null");
        }

        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return encoder.matches(password, this.password);
    }

    private String hashPassword(String password) {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return encoder.encode(password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return username.equals(account.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
