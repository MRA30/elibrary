package com.elibrary.utils;

import com.elibrary.Constans;
import com.elibrary.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    

    public String extractName(String token) {
        String getSubjectAll = extractClaim(token, Claims::getSubject);
        String[] jwtSubject = getSubjectAll.split(",");
        return jwtSubject[2];
    }

    public Integer extractId(String token) {
        String getSubjectAll = extractClaim(token, Claims::getSubject);
        String[] jwtSubject = getSubjectAll.split(",");
        return Integer.parseInt(jwtSubject[0]); 
    }

    public String extractEmail(String token) {
        String getSubjectAll = extractClaim(token, Claims::getSubject);
        String[] jwtSubject = getSubjectAll.split(",");
        return jwtSubject[3];
    }

    public String extractNumberIdentity(String token) {
        String getSubjectAll = extractClaim(token, Claims::getSubject);
        String[] jwtSubject = getSubjectAll.split(",");
        return jwtSubject[1];
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(Constans.SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public String generateToken(User user) {
        String fullName = user.getFirstName() + " " + user.getLastName();
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getId() + "," + user.getNumberIdentity() + "," + fullName +  "," + user.getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(Constans.TIME)).setExpiration(new Date(Constans.TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS256, Constans.SECRET_KEY).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername())  && !isTokenExpired(token));
    }
    
}
