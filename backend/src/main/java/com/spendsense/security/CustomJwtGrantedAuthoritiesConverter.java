package com.spendsense.security;

import com.spendsense.model.User;
import com.spendsense.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Autowired
    private UserService userService;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // extract clerkuid from jwt
        String clerkUserId = jwt.getSubject();

        User user = userService.getOrCreateUser(clerkUserId);
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Handle null role for new users
        String role = user.getRole() != null ? user.getRole().name() : "USER";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        return authorities;
    }

}
