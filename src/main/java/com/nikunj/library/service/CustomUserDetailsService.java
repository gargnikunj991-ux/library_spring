package com.nikunj.library.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nikunj.library.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

     @Override
public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {

          return userRepository.findByUsername(username)
            .orElseThrow(() ->
                    new UsernameNotFoundException("User not found"));

}

    
private final UserRepository userRepository;

public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
    
}
