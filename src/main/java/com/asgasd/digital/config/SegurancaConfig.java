package com.asgasd.digital.config;

import com.asgasd.digital.model.Role;
import com.asgasd.digital.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SegurancaConfig {

    private final UsuarioRepository usuarios;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> usuarios.findByEmailIgnoreCase(email)
                .map(usuario -> {
                    List<String> authorities = usuario.getRoles().stream()
                            .map(Role::getNome)
                            .toList();
                    return User.withUsername(usuario.getEmail())
                            .password(usuario.getSenha())
                            .disabled(!usuario.isAtivo())
                            .authorities(authorities.toArray(String[]::new))
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Conta nao encontrada."));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/media/**", "/favicon.ico").permitAll()
                        .requestMatchers("/", "/produtos", "/produto/**", "/carrinho/**",
                                "/contacto", "/consulta", "/consulta/**", "/auth/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/checkout/**", "/conta/**", "/encomenda/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .defaultSuccessUrl("/conta")
                        .failureUrl("/auth/login?erro")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/?saida")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .sessionManagement(sessao -> sessao
                        .sessionFixation().migrateSession()
                        .invalidSessionUrl("/auth/login?expirada"))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .headers(headers -> headers
                        .contentTypeOptions(opcoes -> { })
                        .frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
