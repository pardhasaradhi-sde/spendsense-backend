package com.spendsense.security;

import com.spendsense.exception.ResourceNotFoundException;
import com.spendsense.model.User;
import com.spendsense.repository.UserRepository;
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
    private UserRepository userRepository;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        //extract clerkuid from jwt
        String clerkUserId=jwt.getSubject();

        User user=userRepository.findByClerkUserId(clerkUserId)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));
        List<GrantedAuthority> authorities=new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()));
        return authorities;
    }

}
