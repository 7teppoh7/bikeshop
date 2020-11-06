package ru.labs.userservice.config.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.labs.userservice.entities.Role;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.token.secret")
    private String jwtSecret;

    private final JwtUserDetailsService userDetailsService;

    public JwtTokenProvider(JwtUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public String createToken(String email, Set<Role> roles){
        Set<String> rolesStr = roles.stream().map(Role::name).collect(Collectors.toSet());
        Date date = Date.from(LocalDate.now().plusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setHeaderParam("type", "jwt")
                .setSubject(email)
                .claim("roles", rolesStr)
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.info("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            log.info("Unsupported jwt");
        } catch (MalformedJwtException mjEx) {
            log.info("Malformed jwt");
        } catch (SignatureException sEx) {
            log.info("Invalid signature");
        } catch (Exception e) {
            log.info("invalid token");
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
