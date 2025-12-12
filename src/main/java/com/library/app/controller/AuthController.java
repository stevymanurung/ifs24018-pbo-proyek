package com.library.app.controller;

import com.library.app.entity.User;
import com.library.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Menampilkan Halaman Register
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Memproses Data Register
    @PostMapping("/register/save")
    public String processRegister(User user) {
        // Enkripsi password sebelum disimpan
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Default Role = USER (Bukan Admin)
        user.setRole("ROLE_USER");
        
        userRepository.save(user);
        return "redirect:/login?success"; // Balik ke login dengan pesan sukses
    }
}