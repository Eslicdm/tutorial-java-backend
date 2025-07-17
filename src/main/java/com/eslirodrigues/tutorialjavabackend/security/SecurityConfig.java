package com.eslirodrigues.tutorialjavabackend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${BASIC_DB_USERNAME}")
    private String ownerUsername;

    @Value("${BASIC_DB_PASSWORD}")
    private String ownerPassword;

    @Value("${BASIC_DB_GUESTNAME}")
    private String guestUsername;

    @Value("${BASIC_DB_GUESTPW}")
    private String guestPassword;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> requests
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/members/**").hasRole("OWNER")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService inMemoryUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();

        UserDetails owner = users
                .username(ownerUsername)
                .password(passwordEncoder.encode(ownerPassword))
                .roles("OWNER")
                .build();

        UserDetails guest = users
                .username(guestUsername)
                .password(passwordEncoder.encode(guestPassword))
                .roles("GUEST")
                .build();

        return new InMemoryUserDetailsManager(owner, guest);
    }
}