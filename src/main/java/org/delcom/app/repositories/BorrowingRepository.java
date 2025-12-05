package org.delcom.app.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, UUID> {
    
    @Query("SELECT br FROM Borrowing br WHERE (LOWER(br.borrowerName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(br.borrowerEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(br.status) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND br.userId = :userId ORDER BY br.createdAt DESC")
    List<Borrowing> findByKeyword(UUID userId, String keyword);

    @Query("SELECT br FROM Borrowing br WHERE br.userId = :userId ORDER BY br.createdAt DESC")
    List<Borrowing> findAllByUserId(UUID userId);

    @Query("SELECT br FROM Borrowing br WHERE br.id = :id AND br.userId = :userId")
    Optional<Borrowing> findByUserIdAndId(UUID userId, UUID id);

    @Query("SELECT br FROM Borrowing br WHERE br.bookId = :bookId AND br.userId = :userId ORDER BY br.createdAt DESC")
    List<Borrowing> findByUserIdAndBookId(UUID userId, UUID bookId);

    @Query("SELECT br FROM Borrowing br WHERE br.status = :status AND br.userId = :userId ORDER BY br.createdAt DESC")
    List<Borrowing> findByUserIdAndStatus(UUID userId, String status);

    @Query("SELECT br FROM Borrowing br WHERE br.dueDate < :date AND br.status = 'BORROWED' AND br.userId = :userId")
    List<Borrowing> findOverdueBorrowings(UUID userId, LocalDate date);

    @Query("SELECT COUNT(br) FROM Borrowing br WHERE br.userId = :userId AND br.status = 'BORROWED'")
    Long countActiveBorrowings(UUID userId);

    @Query("SELECT COUNT(br) FROM Borrowing br WHERE br.userId = :userId AND br.status = 'RETURNED'")
    Long countReturnedBorrowings(UUID userId);
}