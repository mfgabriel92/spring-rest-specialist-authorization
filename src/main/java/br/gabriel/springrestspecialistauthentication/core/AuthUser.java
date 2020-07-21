package br.gabriel.springrestspecialistauthentication.core;

import br.gabriel.springrestspecialistauthentication.domain.TheUser;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
public class AuthUser extends User {
    private final String name;

    public AuthUser(TheUser theUser) {
        super(theUser.getEmail(), theUser.getPassword(), Collections.emptyList());

        this.name = theUser.getName();
    }
}
