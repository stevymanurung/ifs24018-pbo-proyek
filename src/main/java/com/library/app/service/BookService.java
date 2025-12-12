package com.library.app.service;

import com.library.app.entity.Book;
import com.library.app.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // Folder penyimpanan gambar
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public List<Book> getAllBooks(String keyword) {
        if (keyword != null) return bookRepository.findByTitleContainingIgnoreCase(keyword);
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public void saveBook(Book book, MultipartFile file) throws IOException {
        // Logika Upload Gambar (Poin 4)
        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            book.setCoverImage(fileName);
        } else if (book.getId() != null) {
            // Jika edit tapi tidak ganti gambar, pertahankan gambar lama
            Book existing = getBookById(book.getId());
            book.setCoverImage(existing.getCoverImage());
        }
        bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public List<Object[]> getChartData() {
        return bookRepository.countBooksByCategory();
    }
    
}
