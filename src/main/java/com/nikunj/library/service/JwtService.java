package com.nikunj.library.service;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Service
public class JwtService {

    private static final String SECRET_KEY =
        "5F05gEkmG5Gxi6GHqehXUFlsusNdoO0tXnwuK1iUVpQ=";

private static final long EXPIRATION_TIME =
        1000 * 60 * 10; // 10 mintues

        private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
}
public String generateToken(UserDetails userDetails) {

    return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSignInKey())
            .compact();
}
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}
public <T> T extractClaim(String token,
                          Function<Claims, T> claimsResolver) {

    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
}
private Claims extractAllClaims(String token) {

    return Jwts.parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
}
public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
}
private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
}
public boolean isTokenValid(String token, UserDetails userDetails) {

    final String username = extractUsername(token);

    return username.equals(userDetails.getUsername())
            && !isTokenExpired(token);
}
}