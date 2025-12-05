package org.delcom.app.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "borrowings")
public class Borrowing {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    // =================================================================
    // FIELD TAMBAHAN (TRANSIENT) UNTUK TAMPILAN HTML
    // Field ini tidak akan dibuat kolomnya di Database
    // =================================================================
    
    @Transient
    private String bookTitle;

    @Transient
    private String bookAuthor;

    @Transient
    private String bookIsbn;

    @Transient
    private boolean isOverdue;

    // =================================================================

    @Column(name = "borrower_name", nullable = false)
    private String borrowerName;

    @Column(name = "borrower_email", nullable = true)
    private String borrowerEmail;

    @Column(name = "borrower_phone", nullable = true)
    private String borrowerPhone;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date", nullable = true)
    private LocalDate returnDate;

    @Column(name = "status", nullable = false)
    private String status; // BORROWED, RETURNED, OVERDUE

    @Column(name = "notes", nullable = true, length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Borrowing() {
    }

    public Borrowing(UUID userId, UUID bookId, String borrowerName, String borrowerEmail, 
                     String borrowerPhone, LocalDate borrowDate, LocalDate dueDate, String notes) {
        this.userId = userId;
        this.bookId = bookId;
        this.borrowerName = borrowerName;
        this.borrowerEmail = borrowerEmail;
        this.borrowerPhone = borrowerPhone;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = "BORROWED";
        this.notes = notes;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    // --- GETTER SETTER KHUSUS TRANSIENT ---

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    // Logic otomatis untuk mengecek apakah terlambat
    public boolean getIsOverdue() {
        // Jika sudah dikembalikan, tidak terlambat
        if (this.returnDate != null) {
            return false; 
        }
        // Jika belum dikembalikan, cek apakah hari ini melewati due_date
        return LocalDate.now().isAfter(this.dueDate);
    }
    
    public void setIsOverdue(boolean isOverdue) {
        this.isOverdue = isOverdue;
    }

    // --------------------------------------

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getBorrowerEmail() {
        return borrowerEmail;
    }

    public void setBorrowerEmail(String borrowerEmail) {
        this.borrowerEmail = borrowerEmail;
    }

    public String getBorrowerPhone() {
        return borrowerPhone;
    }

    public void setBorrowerPhone(String borrowerPhone) {
        this.borrowerPhone = borrowerPhone;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}