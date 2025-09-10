package com.site.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.site.lms.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          // CSRF 비활성화 (필요시 다시 활성화하세요)
          .csrf(csrf -> csrf.disable())
          
          // URL 접근 권한 설정
          .authorizeHttpRequests(authz -> authz
              // (1) 누구나 접근 가능한 URL
              .requestMatchers(
                  "/", 
                  "/login", 
                  "/register",
                  "/toAdmin",
                  "/api/auth/register",
                  "/api/auth/login-info",
                  "/error",
                  "/css/**", "/js/**", "/images/**",
                  "/uploads/**"
              ).permitAll()

              // (2) 메시지 센터: 로그인한 사용자
              .requestMatchers("/user/messages", "/user/sendMessage").authenticated()

              // (3) 강사(ROLE_USER)도 과목 추가 페이지 접근 허용
              .requestMatchers("/admin/lectures/new").hasRole("USER")

              // (4) /admin/** 은 오직 관리자(ROLE_ADMIN)만
              .requestMatchers("/admin/**").hasRole("ADMIN")

              // (5) 그 외 모든 요청은 인증만 되면 허용
              .anyRequest().authenticated()
          )

          // 로그인 설정
          .formLogin(form -> form
              .loginPage("/login")
              .defaultSuccessUrl("/", true)
              .permitAll()
          )

          // 로그아웃 설정
          .logout(logout -> logout
              .logoutUrl("/logout")
              .logoutSuccessUrl("/")
              .invalidateHttpSession(true)
              .deleteCookies("JSESSIONID")
              .permitAll()
          )

          // 인증 공급자 설정 (DAO + BCrypt)
          .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    protected UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
          .map(u -> org.springframework.security.core.userdetails.User
                      .withUsername(u.getUsername())
                      .password(u.getPassword())
                      // authority == 0 → ROLE_ADMIN, 그 외 → ROLE_USER
                      .roles(u.getAuthority() == 0 ? "ADMIN" : "USER")
                      .build()
           )
          .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
