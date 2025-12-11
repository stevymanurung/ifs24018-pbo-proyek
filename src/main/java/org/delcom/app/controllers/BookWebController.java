package org.delcom.app.controllers;

import java.util.UUID; // WAJIB ADA: Untuk mengubah String ke UUID

import org.delcom.app.entities.Book;
import org.delcom.app.repositories.BookRepository; // TAMBAHKAN INI
import org.delcom.app.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/books")
public class BookWebController {

    @Autowired
    private BookService bookService;

    // TAMBAHKAN INI: Kita butuh repository untuk cari buku tanpa ribet soal User ID dulu
    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        try {
            // 1. Ubah String ID dari URL menjadi UUID
            UUID uuid = UUID.fromString(id);

            // 2. Gunakan repository langsung (bukan service) agar error "undefined" hilang
            Book book = bookRepository.findById(uuid).orElse(null);

            // 3. Cek apakah buku ketemu
            if (book == null) {
                return "redirect:/books";
            }

            // 4. Kirim data ke HTML
            model.addAttribute("book", book);
            
            // Sesuaikan path ini dengan folder templates kamu
            return "models/books/edit";

        } catch (IllegalArgumentException e) {
            // Jika ID di URL error/bukan UUID, kembalikan ke home
            return "redirect:/books";
        }
    }
}