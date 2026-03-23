package com.menamiit.smartcityproject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.menamiit.smartcityproject.security.JwtAuthFilter;
import com.menamiit.smartcityproject.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Static frontend pages do client-side role checks after login.
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/dashboard.html",
                                "/Dashboard.html",
                                "/submit.html",
                                "/Submit.html",
                                "/mygrievances.html",
                                "/MyGrievances.html",
                                "/citizenprofile.html",
                                "/CitizenProfile.html",
                                "/citizennotifications.html",
                                "/CitizenNotifications.html",
                                "/admin.html",
                                "/admingrievances.html",
                                "/AdminGrievances.html",
                                "/manageusers.html",
                                "/ManageUsers.html",
                                "/assignofficers.html",
                                "/AssignOfficers.html",
                                "/officerassignedtasks.html",
                                "/adminanalytics.html",
                                "/AdminAnalytics.html",
                                "/adminsettings.html",
                                "/AdminSettings.html",
                                "/officeranalytics.html",
                                "/OfficerAnalytics.html",
                                "/analytics-common.js",
                                "/OfficerAssignedTasks.html",
                                "/officerinprogress.html",
                                "/OfficerInProgress.html",
                                "/officercompleted.html",
                                "/OfficerCompleted.html",
                                "/officerprofile.html",
                                "/OfficerProfile.html",
                                "/officer-pages.css",
                                "/officer-common.js",
                                "/dashboard",
                                "/submit",
                                "/mygrievances",
                                "/favicon.ico",
                                "/h2-console/**",
                                "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        // ── Auth API — open ────────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        // ── Grievance API — JWT validated in filter ────────────────
                        .requestMatchers("/api/grievances/**").permitAll()
                        // ── Admin API — JWT validated in controller ────────────────
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/officer/**").hasRole("OFFICER")
                        .requestMatchers("/api/analytics/**").hasAnyRole("ADMIN", "OFFICER")
                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}