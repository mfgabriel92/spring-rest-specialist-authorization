package br.gabriel.springrestspecialistauthentication.core;

import br.gabriel.springrestspecialistauthentication.domain.TheUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class AuthUser extends User {
    private final Integer id;

    private final String name;

    public AuthUser(TheUser theUser, Collection<? extends GrantedAuthority> authorities) {
        super(theUser.getEmail(), theUser.getPassword(), authorities);

        this.id = theUser.getId();
        this.name = theUser.getName();
    }
}
