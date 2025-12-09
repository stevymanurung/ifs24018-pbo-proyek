package org.delcom.app.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Book;
import org.delcom.app.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final FileStorageService fileStorageService;

    public BookService(BookRepository bookRepository, FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Book createBook(UUID userId, String title, String author, String isbn, 
                          String category, String publisher, Integer publicationYear, Integer stock) {
        // Check if ISBN already exists for this user
        Optional<Book> existingBook = bookRepository.findByUserIdAndIsbn(userId, isbn);
        if (existingBook.isPresent()) {
            return null; // ISBN already exists
        }

        Book book = new Book(userId, title, author, isbn, category, publisher, publicationYear, stock);
        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<Book> getAllBooks(UUID userId, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return bookRepository.findByKeyword(userId, search.trim());
        }
        return bookRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Book getBookById(UUID userId, UUID id) {
        return bookRepository.findByUserIdAndId(userId, id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksByCategory(UUID userId, String category) {
        return bookRepository.findByUserIdAndCategory(userId, category);
    }

    @Transactional
    public Book updateBook(UUID userId, UUID id, String title, String author, String isbn,
                          String category, String publisher, Integer publicationYear, Integer stock) {
        Book book = bookRepository.findByUserIdAndId(userId, id).orElse(null);
        if (book == null) {
            return null; // Book not found
        }

        // Check if new ISBN conflicts with another book
        if (!book.getIsbn().equals(isbn)) {
            Optional<Book> existingBook = bookRepository.findByUserIdAndIsbn(userId, isbn);
            if (existingBook.isPresent() && !existingBook.get().getId().equals(id)) {
                return null; // ISBN already used by another book
            }
        }

        // Update book details
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setPublicationYear(publicationYear);
        
        // Update stock and available stock proportionally
        int stockDifference = stock - book.getStock();
        book.setStock(stock);
        book.setAvailableStock(Math.max(0, book.getAvailableStock() + stockDifference));
        
        return bookRepository.save(book);
    }

    @Transactional
    public boolean deleteBook(UUID userId, UUID id) {
        Book book = bookRepository.findByUserIdAndId(userId, id).orElse(null);
        if (book == null) {
            return false;
        }

        // Delete cover file if exists
        if (book.getCover() != null && !book.getCover().isEmpty()) {
            fileStorageService.deleteFile(book.getCover());
        }

        bookRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Book updateCover(UUID userId, UUID bookId, String coverFilename) {
        Book book = bookRepository.findByUserIdAndId(userId, bookId).orElse(null);
        if (book == null) {
            return null;
        }

        // Delete old cover if exists
        if (book.getCover() != null && !book.getCover().isEmpty()) {
            fileStorageService.deleteFile(book.getCover());
        }

        book.setCover(coverFilename);
        return bookRepository.save(book);
    }

    // Additional methods for statistics
    @Transactional(readOnly = true)
    public List<String> getAllCategories(UUID userId) {
        return bookRepository.findDistinctCategoriesByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Long countBooksByCategory(UUID userId, String category) {
        return bookRepository.countByUserIdAndCategory(userId, category);
    }
}