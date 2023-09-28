package com.example.demoproject.config.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;
import com.example.demoproject.domain.user.Role;
import lombok.RequiredArgsConstructor;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(header -> header.frameOptions(FrameOptionsConfig::disable))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    antMatcher("/"),
                    antMatcher("/css/**"),
                    antMatcher("/images/**"),
                    antMatcher("/js/**"),
                    antMatcher("/h2-console/**"),
                    antMatcher("/profile")).permitAll()
                .requestMatchers(
                    antMatcher("/api/v1/**")).hasRole(Role.USER.name())
                .anyRequest().authenticated()
            )
            .logout(logout -> logout.logoutSuccessUrl("/"))
            .oauth2Login(oauth -> oauth.userInfoEndpoint(
                endPoint -> endPoint.userService(customOAuth2UserService)
            ));

        return http.build();
    }
}
