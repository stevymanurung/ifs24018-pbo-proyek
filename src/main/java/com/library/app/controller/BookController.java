package com.library.app.controller;

import com.library.app.entity.Book;
import com.library.app.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
// Import PENTING yang kurang:
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.*; // Ini untuk @PathVariable, @GetMapping, @PostMapping
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    // Poin 8: Dashboard & Chart
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("chartData", bookService.getChartData());
        return "index";
    }

    // Poin 6: Daftar Data & Search
    @GetMapping("/books")
    public String listBooks(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("books", bookService.getAllBooks(keyword));
        return "books";
    }

    // Poin 2: Form Tambah
    @GetMapping("/book/new")
    public String createBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "form";
    }

    // Poin 2, 3, 4: Proses Simpan/Update dengan Gambar
    @PostMapping("/book/save")
    public String saveBook(@ModelAttribute Book book, 
                           @RequestParam("image") MultipartFile file) throws IOException {
        bookService.saveBook(book, file);
        return "redirect:/books";
    }

    // Poin 3: Form Edit
    @GetMapping("/book/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        return "form";
    }

    // Poin 7: Detail Data
    @GetMapping("/book/detail/{id}")
    public String detailBook(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        return "detail";
    }

    // Poin 5: Hapus Data
    @GetMapping("/book/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }


    
} 
    

