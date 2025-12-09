package org.delcom.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Book;
import org.delcom.app.entities.User;
import org.delcom.app.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    @Autowired
    private AuthContext authContext;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, UUID>>> createBook(@RequestBody Book reqBook) {
        // Validation
        if (reqBook.getTitle() == null || reqBook.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Title tidak boleh kosong", null));
        }
        if (reqBook.getAuthor() == null || reqBook.getAuthor().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Author tidak boleh kosong", null));
        }
        if (reqBook.getIsbn() == null || reqBook.getIsbn().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "ISBN tidak boleh kosong", null));
        }
        if (reqBook.getCategory() == null || reqBook.getCategory().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Category tidak boleh kosong", null));
        }
        if (reqBook.getStock() == null || reqBook.getStock() <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Stock harus lebih dari 0", null));
        }

        // Authentication check
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        // Create book
        Book newBook = bookService.createBook(
                authUser.getId(),
                reqBook.getTitle().trim(),
                reqBook.getAuthor().trim(),
                reqBook.getIsbn().trim(),
                reqBook.getCategory().trim(),
                reqBook.getPublisher() != null ? reqBook.getPublisher().trim() : null,
                reqBook.getPublicationYear(),
                reqBook.getStock());

        if (newBook == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("fail", "ISBN sudah terdaftar", null));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Buku berhasil ditambahkan", 
                      Map.of("id", newBook.getId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllBooks(
            @RequestParam(required = false) String search) {
        
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<Book> books = bookService.getAllBooks(authUser.getId(), search);
        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Daftar buku berhasil diambil",
                Map.of("books", books, "total", books.size())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Book>>> getBookById(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Book book = bookService.getBookById(authUser.getId(), id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("fail", "Buku tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Data buku berhasil diambil",
                Map.of("book", book)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Book>>> updateBook(
            @PathVariable UUID id, 
            @RequestBody Book reqBook) {
        
        // Validation
        if (reqBook.getTitle() == null || reqBook.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Title tidak boleh kosong", null));
        }
        if (reqBook.getAuthor() == null || reqBook.getAuthor().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Author tidak boleh kosong", null));
        }
        if (reqBook.getIsbn() == null || reqBook.getIsbn().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "ISBN tidak boleh kosong", null));
        }
        if (reqBook.getCategory() == null || reqBook.getCategory().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Category tidak boleh kosong", null));
        }
        if (reqBook.getStock() == null || reqBook.getStock() <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("fail", "Stock harus lebih dari 0", null));
        }

        // Authentication check
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        // Update book
        Book updatedBook = bookService.updateBook(
                authUser.getId(),
                id,
                reqBook.getTitle().trim(),
                reqBook.getAuthor().trim(),
                reqBook.getIsbn().trim(),
                reqBook.getCategory().trim(),
                reqBook.getPublisher() != null ? reqBook.getPublisher().trim() : null,
                reqBook.getPublicationYear(),
                reqBook.getStock());

        if (updatedBook == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("fail", "Buku tidak ditemukan atau ISBN sudah digunakan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success", 
                "Data buku berhasil diperbarui", 
                Map.of("book", updatedBook)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        boolean status = bookService.deleteBook(authUser.getId(), id);
        if (!status) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("fail", "Buku tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Buku berhasil dihapus",
                null));
    }

    // Endpoint untuk mendapatkan kategori (untuk chart)
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getCategories() {
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<String> categories = bookService.getAllCategories(authUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Daftar kategori berhasil diambil",
                Map.of("categories", categories)));
    }
}