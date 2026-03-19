package com.moodify.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    public String secret;

    @Value("${jwt.expiration}")
    public String expiration;


    //Key generation-->C Uses HMAC-SHA algorithm for cryptographic signing-->return Key object for JWT signing and validation
    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    //Token Parsing Methods --> Extracts the username (subject) from a JWT token.-->The subject typically stores the user's identifier (email/username)-->-->return The username stored in the token
    public String extractUsername(String token){
        return extractClaim(token,claims -> claims.getSubject());
    }

    //Extracts the expiration date from a JWT token -->Used to check if token is still valid --> return Date when the token expires
    public Date extractExpiration(String token){
        return  extractClaim(token,claims-> claims.getExpiration());
    }

    // Generic method to extract any claim from a JWT token.-->
    /**
     * Uses a Function to specify which claim to extract
     *
     * @param <T> The type of the claim to extract
     * @param token The JWT token string
     * @param claimsResolver Function that defines which claim to extract
     * @return The extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final  Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

