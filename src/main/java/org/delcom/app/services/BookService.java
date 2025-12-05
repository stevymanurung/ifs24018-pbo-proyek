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
        // Check if ISBN already exists
        Optional<Book> existingBook = bookRepository.findByUserIdAndIsbn(userId, isbn);
        if (existingBook.isPresent()) {
            return null; // ISBN sudah ada
        }

        Book book = new Book(userId, title, author, isbn, category, publisher, publicationYear, stock);
        return bookRepository.save(book);
    }

    public List<Book> getAllBooks(UUID userId, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return bookRepository.findByKeyword(userId, search);
        }
        return bookRepository.findAllByUserId(userId);
    }

    public Book getBookById(UUID userId, UUID id) {
        return bookRepository.findByUserIdAndId(userId, id).orElse(null);
    }

    public List<Book> getBooksByCategory(UUID userId, String category) {
        return bookRepository.findByUserIdAndCategory(userId, category);
    }

    public List<Book> getAvailableBooks(UUID userId) {
        return bookRepository.findAvailableBooks(userId);
    }

    @Transactional
    public Book updateBook(UUID userId, UUID id, String title, String author, String isbn,
                          String category, String publisher, Integer publicationYear, Integer stock) {
        Book book = bookRepository.findByUserIdAndId(userId, id).orElse(null);
        if (book != null) {
            // Check if new ISBN conflicts with another book
            if (!book.getIsbn().equals(isbn)) {
                Optional<Book> existingBook = bookRepository.findByUserIdAndIsbn(userId, isbn);
                if (existingBook.isPresent()) {
                    return null; // ISBN sudah digunakan buku lain
                }
            }

            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            book.setCategory(category);
            book.setPublisher(publisher);
            book.setPublicationYear(publicationYear);
            
            // Update available stock proportionally
            int stockDifference = stock - book.getStock();
            book.setStock(stock);
            book.setAvailableStock(book.getAvailableStock() + stockDifference);
            
            return bookRepository.save(book);
        }
        return null;
    }

    @Transactional
    public boolean deleteBook(UUID userId, UUID id) {
        Book book = bookRepository.findByUserIdAndId(userId, id).orElse(null);
        if (book == null) {
            return false;
        }

        // Hapus cover jika ada
        if (book.getCover() != null) {
            fileStorageService.deleteFile(book.getCover());
        }

        bookRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Book updateCover(UUID bookId, String coverFilename) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();

            // Hapus file cover lama jika ada
            if (book.getCover() != null) {
                fileStorageService.deleteFile(book.getCover());
            }

            book.setCover(coverFilename);
            return bookRepository.save(book);
        }
        return null;
    }

    @Transactional
    public boolean decreaseStock(UUID bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.getAvailableStock() > 0) {
                book.setAvailableStock(book.getAvailableStock() - 1);
                bookRepository.save(book);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean increaseStock(UUID bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.getAvailableStock() < book.getStock()) {
                book.setAvailableStock(book.getAvailableStock() + 1);
                bookRepository.save(book);
                return true;
            }
        }
        return false;
    }
}