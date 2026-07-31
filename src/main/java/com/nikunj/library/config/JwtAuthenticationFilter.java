package com.nikunj.library.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import com.nikunj.library.service.CustomUserDetailsService;
import com.nikunj.library.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtService jwtService;
private final CustomUserDetailsService userDetailsService;

public JwtAuthenticationFilter(
        JwtService jwtService,
        CustomUserDetailsService userDetailsService) {

    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
}

@Override
protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain)
    throws ServletException, IOException {
        System.out.println("JWT FILTER RUNNING");
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token found in request");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7).trim();
            System.out.println("JWT token found, extracting username...");
            final String username = jwtService.extractUsername(jwt);
            System.out.println("Extracted username: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authentication set successfully for user: " + username);
                } else {
                    System.out.println("Token validation failed for user: " + username);
                }
            }
        } catch (Exception e) {
            System.out.println("JWT processing error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
}
}