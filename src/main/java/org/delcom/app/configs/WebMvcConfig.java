package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // Terapkan ke semua URL
                .excludePathPatterns(
                        // Daftar URL yang BOLEH diakses TANPA LOGIN:
                        "/auth/**", 
                        "/login", 
                        "/register", 
                        "/", 
                        "/index",
                        
                        // File statis (CSS, JS, Gambar) agar tampilan tidak rusak:
                        "/assets/**", 
                        "/css/**", 
                        "/js/**", 
                        "/images/**", 
                        "/vendor/**",
                        
                        // PENTING: Izinkan akses ke books sementara agar tombol edit bisa diklik
                        "/books/**"
                );
    }
}