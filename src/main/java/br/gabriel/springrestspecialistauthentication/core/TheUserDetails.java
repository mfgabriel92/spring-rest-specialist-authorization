package br.gabriel.springrestspecialistauthentication.core;

import br.gabriel.springrestspecialistauthentication.domain.TheUser;
import br.gabriel.springrestspecialistauthentication.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class TheUserDetails implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        TheUser theUser = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new AuthUser(theUser, getAuthorities(theUser));
    }

    private Collection<GrantedAuthority> getAuthorities(TheUser theUser) {
        return theUser.getGroups().stream()
            .flatMap(group -> group.getPermissions()
                .stream().map(permission -> new SimpleGrantedAuthority(permission.getName().toUpperCase())))
            .collect(Collectors.toSet());
    }
}
