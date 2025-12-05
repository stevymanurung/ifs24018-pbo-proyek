package org.delcom.app.dto;

import java.time.LocalDate;
import java.util.UUID;

public class BorrowingForm {

    private UUID id;
    private UUID bookId;
    private String borrowerName;
    private String borrowerEmail;
    private String borrowerPhone;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private String notes;
    private String confirmBorrowerName;

    // Constructor
    public BorrowingForm() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getConfirmBorrowerName() {
        return confirmBorrowerName;
    }

    public void setConfirmBorrowerName(String confirmBorrowerName) {
        this.confirmBorrowerName = confirmBorrowerName;
    }
} 