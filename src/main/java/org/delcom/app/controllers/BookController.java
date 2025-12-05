package org.delcom.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Book;
import org.delcom.app.entities.User;
import org.delcom.app.services.BookService;
import org.delcom.app.services.BorrowingService; // Tambahkan service ini untuk statistik
import org.delcom.app.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookController {

    private final BookService bookService;
    private final BorrowingService borrowingService; 

    @Autowired
    protected AuthContext authContext;

    public BookController(BookService bookService, BorrowingService borrowingService) {
        this.bookService = bookService;
        this.borrowingService = borrowingService;
    }

    // ========================================================================
    // VIEW ENDPOINTS (Mengembalikan HTML)
    // ========================================================================

    // 1. DASHBOARD / HOME
    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) boolean addBookModalOpen,
                       @RequestParam(required = false) boolean editBookModalOpen,
                       @RequestParam(required = false) boolean deleteBookModalOpen,
                       @RequestParam(required = false) UUID editBookModalId,
                       @RequestParam(required = false) UUID deleteBookModalId) {
        
        if (!authContext.isAuthenticated()) return "redirect:/" + ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        User authUser = authContext.getAuthUser();

        // Data Statistik untuk Dashboard
        model.addAttribute("totalBooks", bookService.getAllBooks(authUser.getId(), null).size());
        model.addAttribute("activeBorrowings", borrowingService.getBorrowingsByStatus(authUser.getId(), "DIPINJAM").size());
        model.addAttribute("returnedBorrowings", borrowingService.getBorrowingsByStatus(authUser.getId(), "DIKEMBALIKAN").size());

        // Data List Buku
        model.addAttribute("books", bookService.getAllBooks(authUser.getId(), search));
        model.addAttribute("search", search);
        model.addAttribute("auth", authUser);

        // Logika Membuka Modal (Sesuai parameter URL)
        if (addBookModalOpen) {
            model.addAttribute("addBookModalOpen", true);
            model.addAttribute("bookForm", new Book());
        }
        if (editBookModalOpen && editBookModalId != null) {
            Book book = bookService.getBookById(authUser.getId(), editBookModalId);
            if(book != null) {
                model.addAttribute("editBookModalOpen", true);
                model.addAttribute("editBookModalId", editBookModalId);
                model.addAttribute("bookForm", book);
            }
        }
        if (deleteBookModalOpen && deleteBookModalId != null) {
            model.addAttribute("deleteBookModalOpen", true);
            model.addAttribute("deleteBookModalId", deleteBookModalId);
            model.addAttribute("bookForm", new Book());
        }

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }

    // 2. DETAIL BUKU
    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable UUID id, Model model) {
        if (!authContext.isAuthenticated()) return "redirect:/" + ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        
        Book book = bookService.getBookById(authContext.getAuthUser().getId(), id);
        if (book == null) return "redirect:/";

        model.addAttribute("book", book);
        model.addAttribute("auth", authContext.getAuthUser());
        // Form dummy untuk modal jika dipanggil dari detail page
        model.addAttribute("bookForm", book); 

        return ConstUtil.TEMPLATE_PAGES_BOOKS_DETAIL;
    }

    // 3. PROSES TAMBAH BUKU (Form Submit)
    @PostMapping("/books/add")
    public String createBook(@ModelAttribute Book reqBook, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) return "redirect:/login";
        
        // Simpan File Gambar (Implementasi sederhana)
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename(); 
         
            reqBook.setCover(fileName); // Asumsi di Entity Book ada setCover
        }

        Book newBook = bookService.createBook(
                authContext.getAuthUser().getId(),
                reqBook.getTitle(),
                reqBook.getAuthor(),
                reqBook.getIsbn(),
                reqBook.getCategory(),
                reqBook.getPublisher(),
                reqBook.getPublicationYear(),
                reqBook.getStock());

        if (newBook == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambah buku. ISBN mungkin duplikat.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Buku berhasil ditambahkan.");
        }

        return "redirect:/";
    }

    // 4. PROSES EDIT BUKU (Form Submit)
    @PostMapping("/books/edit")
    public String updateBook(@ModelAttribute Book reqBook, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) return "redirect:/login";

        // Logic update gambar jika ada file baru
        if (!file.isEmpty()) {
         
        }

        bookService.updateBook(
                authContext.getAuthUser().getId(),
                reqBook.getId(),
                reqBook.getTitle(),
                reqBook.getAuthor(),
                reqBook.getIsbn(),
                reqBook.getCategory(),
                reqBook.getPublisher(),
                reqBook.getPublicationYear(),
                reqBook.getStock());

        redirectAttributes.addFlashAttribute("success", "Data buku berhasil diperbarui.");
        return "redirect:/";
    }

    // 5. PROSES HAPUS BUKU (Form Submit)
    @PostMapping("/books/delete")
    public String deleteBook(@ModelAttribute Book reqBook, RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) return "redirect:/login";

        bookService.deleteBook(authContext.getAuthUser().getId(), reqBook.getId());
        redirectAttributes.addFlashAttribute("success", "Buku berhasil dihapus.");
        
        return "redirect:/";
    }

    // ========================================================================
    // API ENDPOINTS (JSON) - Wajib ada untuk Chart.js di home.html
    // ========================================================================

    @GetMapping("/api/books")
    @ResponseBody 
    public ResponseEntity<ApiResponse<Map<String, List<Book>>>> getAllBooksApi(@RequestParam(required = false) String search) {
        if (!authContext.isAuthenticated()) return ResponseEntity.status(403).build();

        List<Book> books = bookService.getAllBooks(authContext.getAuthUser().getId(), search);
        return ResponseEntity.ok(new ApiResponse<>("success", "Data fetched", Map.of("books", books)));
    }

    @GetMapping("/api/books/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Book>>> getBookByIdApi(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) return ResponseEntity.status(403).build();

        Book book = bookService.getBookById(authContext.getAuthUser().getId(), id);
        return ResponseEntity.ok(new ApiResponse<>("success", "Data fetched", Map.of("book", book)));
    }
}