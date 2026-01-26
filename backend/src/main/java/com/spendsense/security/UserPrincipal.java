package com.spendsense.security;

import com.spendsense.exception.UnauthorizedException;
import com.spendsense.model.User;
import com.spendsense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPrincipal {
    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        if(authentication==null || !(authentication.getPrincipal() instanceof Jwt jwt)){
            throw new UnauthorizedException("No authenticated User");
        }
        String clerkUserId=jwt.getSubject();
        return userRepository.findByClerkUserId(clerkUserId)
                .orElseThrow(()->new UnauthorizedException("User not found"));
    }
}
