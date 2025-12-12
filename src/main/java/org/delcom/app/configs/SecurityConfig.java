package org.delcom.app.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Import baru
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. NONAKTIFKAN CSRF
            // Ini sering menjadi penyebab error 403 Forbidden pada metode POST/PUT/DELETE
            // atau jika token tidak terkirim dengan benar.
            .csrf(AbstractHttpConfigurer::disable) 

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.sendRedirect("/auth/login");
                }))
            
            .authorizeHttpRequests(auth -> auth
                // 2. DAFTARKAN URL YANG BOLEH DIAKSES
                // Tambahkan "/books/**" di sini agar semua fitur buku (edit, tambah, lihat)
                // bisa diakses. 
                // Jika ingin HARUS LOGIN, pindahkan "/books/**" ke bawah permitAll().
                .requestMatchers(
                    "/auth/**", 
                    "/assets/**", 
                    "/api/**",
                    "/css/**", 
                    "/js/**",
                    "/books/**" // <--- TAMBAHAN PENTING (Agar aset buku bisa diakses)
                )
                .permitAll() // URL di atas boleh diakses siapa saja (Public)
                
                // Sisa request lainnya WAJIB login
                .anyRequest().authenticated()
            )

            // Catatan: Anda menonaktifkan formLogin standar. 
            // Pastikan Anda punya Controller sendiri di "/auth/login" 
            // yang menangani proses autentikasi manual.
            .formLogin(form -> form.disable())
            
            .logout(logout -> logout
                .logoutSuccessUrl("/auth/login")
                .permitAll())
            
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}