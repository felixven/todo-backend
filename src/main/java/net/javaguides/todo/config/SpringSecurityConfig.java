package net.javaguides.todo.config;

import lombok.AllArgsConstructor;
import net.javaguides.todo.entity.Role;
import net.javaguides.todo.entity.User;
import net.javaguides.todo.repository.RoleRepository;
import net.javaguides.todo.repository.UserRepository;
import net.javaguides.todo.security.JwtAuthenticationEntryPoint;
import net.javaguides.todo.security.JwtAuthenticationFilter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;

@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class SpringSecurityConfig {

    private UserDetailsService userDetailsService;

    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    private JwtAuthenticationFilter authenticationFilter;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf().disable()
                .authorizeHttpRequests((authorize) -> {
//                    authorize.requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "USER");
//                    authorize.requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("ADMIN", "USER");
//                    authorize.requestMatchers(HttpMethod.GET, "/api/**").permitAll();
                    authorize.requestMatchers("/api/auth/**").permitAll();
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    authorize.anyRequest().authenticated();
                }).httpBasic(Customizer.withDefaults());
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint));

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

//    @Bean//spring container to manage the object of this class
//    public UserDetailsService userDetailsService() {
//        UserDetails ven = User.builder()
//                .username("ven")
//                .password(passwordEncoder().encode("password"))
//                .roles("USER")
//                .build();
//
//        UserDetails admin = User.builder()
//                .username("admin")
//                .password(passwordEncoder().encode("admin"))
//                .roles("ADMIN")
//                .build();
//
//        return new InMemoryUserDetailsManager(ven, admin);
//    }


    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder encoder) {
        return args -> {
            // 1. 確保角色存在
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = roleRepository.save(new Role(null, "ROLE_ADMIN"));
            }

            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                userRole = roleRepository.save(new Role(null, "ROLE_USER"));
            }

            // 2. 確保 admin 存在
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User(null, "Admin", "Admin", "admin",
                        "admin@example.com", encoder.encode("admin1234"), new HashSet<>());
                userRepository.save(admin);

                // 3. 存完 user 再加角色，這樣 roles 的 persist 就不會干擾
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
            }
        };
    }





}


