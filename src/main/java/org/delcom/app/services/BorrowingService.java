package org.delcom.app.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.Borrowing;
import org.delcom.app.entities.Book;
import org.delcom.app.repositories.BorrowingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowingService {
    private final BorrowingRepository borrowingRepository;
    private final BookService bookService;

    public BorrowingService(BorrowingRepository borrowingRepository, BookService bookService) {
        this.borrowingRepository = borrowingRepository;
        this.bookService = bookService;
    }

    // ==================================================================================
    // HELPER METHOD: Mengisi data buku ke dalam object Borrowing (untuk field Transient)
    // ==================================================================================
    private void enrichBorrowingData(Borrowing borrowing) {
        if (borrowing != null) {
            Book book = bookService.getBookById(borrowing.getUserId(), borrowing.getBookId());
            if (book != null) {
                borrowing.setBookTitle(book.getTitle());
                borrowing.setBookAuthor(book.getAuthor());
                borrowing.setBookIsbn(book.getIsbn());
            }
        }
    }

    private void enrichBorrowingList(List<Borrowing> borrowings) {
        for (Borrowing borrowing : borrowings) {
            enrichBorrowingData(borrowing);
        }
    }
    // ==================================================================================

    @Transactional
    public Borrowing createBorrowing(UUID userId, UUID bookId, String borrowerName, 
                                    String borrowerEmail, String borrowerPhone, 
                                    LocalDate borrowDate, LocalDate dueDate, String notes) {
        // Check if book is available
        Book book = bookService.getBookById(userId, bookId);
        if (book == null || book.getAvailableStock() <= 0) {
            return null; // Buku tidak tersedia
        }

        // Decrease book stock
        boolean decreased = bookService.decreaseStock(bookId);
        if (!decreased) {
            return null;
        }

        Borrowing borrowing = new Borrowing(userId, bookId, borrowerName, borrowerEmail, 
                                            borrowerPhone, borrowDate, dueDate, notes);
        
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);
        enrichBorrowingData(savedBorrowing); // Enrich sebelum return
        return savedBorrowing;
    }

    public List<Borrowing> getAllBorrowings(UUID userId, String search) {
        List<Borrowing> borrowings;
        if (search != null && !search.trim().isEmpty()) {
            borrowings = borrowingRepository.findByKeyword(userId, search);
        } else {
            borrowings = borrowingRepository.findAllByUserId(userId);
        }
        
        // PENTING: Isi data judul buku agar muncul di HTML
        enrichBorrowingList(borrowings);
        return borrowings;
    }

    public Borrowing getBorrowingById(UUID userId, UUID id) {
        Borrowing borrowing = borrowingRepository.findByUserIdAndId(userId, id).orElse(null);
        enrichBorrowingData(borrowing); // Isi data buku
        return borrowing;
    }

    public List<Borrowing> getBorrowingsByBookId(UUID userId, UUID bookId) {
        List<Borrowing> borrowings = borrowingRepository.findByUserIdAndBookId(userId, bookId);
        enrichBorrowingList(borrowings);
        return borrowings;
    }

    public List<Borrowing> getBorrowingsByStatus(UUID userId, String status) {
        List<Borrowing> borrowings = borrowingRepository.findByUserIdAndStatus(userId, status);
        enrichBorrowingList(borrowings);
        return borrowings;
    }

    public List<Borrowing> getOverdueBorrowings(UUID userId) {
        List<Borrowing> borrowings = borrowingRepository.findOverdueBorrowings(userId, LocalDate.now());
        enrichBorrowingList(borrowings);
        return borrowings;
    }

    @Transactional
    public Borrowing updateBorrowing(UUID userId, UUID id, String borrowerName, 
                                    String borrowerEmail, String borrowerPhone, 
                                    LocalDate borrowDate, LocalDate dueDate, String notes) {
        Borrowing borrowing = borrowingRepository.findByUserIdAndId(userId, id).orElse(null);
        if (borrowing != null && "BORROWED".equals(borrowing.getStatus())) {
            borrowing.setBorrowerName(borrowerName);
            borrowing.setBorrowerEmail(borrowerEmail);
            borrowing.setBorrowerPhone(borrowerPhone);
            borrowing.setBorrowDate(borrowDate);
            borrowing.setDueDate(dueDate);
            borrowing.setNotes(notes);
            
            Borrowing updated = borrowingRepository.save(borrowing);
            enrichBorrowingData(updated);
            return updated;
        }
        return null;
    }

    @Transactional
    public Borrowing returnBook(UUID userId, UUID id, LocalDate returnDate) {
        Borrowing borrowing = borrowingRepository.findByUserIdAndId(userId, id).orElse(null);
        if (borrowing != null && "BORROWED".equals(borrowing.getStatus())) {
            borrowing.setReturnDate(returnDate);
            borrowing.setStatus("RETURNED");
            
            // Increase book stock
            bookService.increaseStock(borrowing.getBookId());
            
            Borrowing saved = borrowingRepository.save(borrowing);
            enrichBorrowingData(saved);
            return saved;
        }
        return null;
    }

    @Transactional
    public boolean deleteBorrowing(UUID userId, UUID id) {
        Borrowing borrowing = borrowingRepository.findByUserIdAndId(userId, id).orElse(null);
        if (borrowing == null) {
            return false;
        }

        // If still borrowed, return the stock
        if ("BORROWED".equals(borrowing.getStatus())) {
            bookService.increaseStock(borrowing.getBookId());
        }

        borrowingRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void updateOverdueStatus(UUID userId) {
        List<Borrowing> overdueBorrowings = borrowingRepository.findOverdueBorrowings(userId, LocalDate.now());
        for (Borrowing borrowing : overdueBorrowings) {
            borrowing.setStatus("OVERDUE");
            borrowingRepository.save(borrowing);
        }
    }

    public Long countActiveBorrowings(UUID userId) {
        return borrowingRepository.countActiveBorrowings(userId);
    }

    public Long countReturnedBorrowings(UUID userId) {
        return borrowingRepository.countReturnedBorrowings(userId);
    }
}