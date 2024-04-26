package com.ra.security.config;


import com.ra.security.jwt.JWTAuthTokenFilter;
import com.ra.security.principle.UserDetailsServiceCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true,securedEnabled = true)
public class SecurityConfig {
    @Autowired
    private JWTAuthTokenFilter authTokenFilter;
    @Autowired
    private SecurityAuthenticationEntryPoint entryPoint;
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }  // mã hóa mk 1 chiều
    @Autowired
    private UserDetailsServiceCustom detailsServiceCustom;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(detailsServiceCustom);
        return provider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
        return auth.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->
//                        auth.requestMatchers("/admin/**").hasRole("ADMIN"))
                        auth.requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/user/**").hasAuthority("ROLE_USER")
                                .requestMatchers("/manager/**").hasAuthority("ROLE_MANAGER")
                                .requestMatchers("/user-manager/**","/user-client/**").hasAnyAuthority("ROLE_USER","ROLE_MANAGER")
                                .anyRequest().permitAll() // tất cả quyền
                )
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
    }


}
