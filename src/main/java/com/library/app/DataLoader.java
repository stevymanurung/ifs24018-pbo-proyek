package com.library.app;

import com.library.app.entity.Book;
import com.library.app.entity.User;
import com.library.app.repository.BookRepository;
import com.library.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Buat User Admin jika belum ada
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            // Password 'admin123' dienkripsi
            admin.setPassword(passwordEncoder.encode("admin123")); 
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
        }

        // 2. Isi Data Buku Dummy (Codingan lama kamu)
        if (bookRepository.count() == 0) {
            Book book1 = new Book();
            book1.setTitle("Belajar Spring Boot");
            book1.setAuthor("Riza");
            book1.setCategory("Teknologi");
            book1.setStock(10);
            bookRepository.save(book1);
        }
    }
}