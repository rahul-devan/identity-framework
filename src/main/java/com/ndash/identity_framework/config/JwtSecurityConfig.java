package com.ndash.identity_framework.config;

import com.ndash.identity_framework.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class JwtSecurityConfig {

    @Bean
    @Profile("prod")
    public JwtDecoder entraJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") final String issuerUri) {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    @Profile("!prod")
    public JwtDecoder localJwtDecoder(@Value("${jwt.secret}") final String secret) {
        final var key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(
            final UserRepository userRepository) {
        final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> resolveAuthorities(jwt, userRepository));
        return converter;
    }

    private Collection<GrantedAuthority> resolveAuthorities(final Jwt jwt, final UserRepository userRepository) {
        final Set<GrantedAuthority> authorities = new HashSet<>();
        final JwtGrantedAuthoritiesConverter defaults = new JwtGrantedAuthoritiesConverter();
        authorities.addAll(defaults.convert(jwt));

        final Long userId = jwt.getClaim("userId");
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> addRoleAuthorities(authorities, user));
        }

        String email = jwt.getClaimAsString("upn");
        if (email == null) {
            email = jwt.getSubject();
        }
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(user -> addRoleAuthorities(authorities, user));
        }

        return new ArrayList<>(authorities);
    }

    private void addRoleAuthorities(final Set<GrantedAuthority> authorities, final com.ndash.identity_framework.domain.User user) {
        user.getUserRoles().forEach(userRole ->
                authorities.add(new SimpleGrantedAuthority(userRole.getRole().getName()))
        );
    }
}
