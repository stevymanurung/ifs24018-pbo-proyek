package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    
    @Query("SELECT b FROM Book b WHERE (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND b.userId = :userId ORDER BY b.createdAt DESC")
    List<Book> findByKeyword(UUID userId, String keyword);

    @Query("SELECT b FROM Book b WHERE b.userId = :userId ORDER BY b.createdAt DESC")
    List<Book> findAllByUserId(UUID userId);

    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.userId = :userId")
    Optional<Book> findByUserIdAndId(UUID userId, UUID id);

    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn AND b.userId = :userId")
    Optional<Book> findByUserIdAndIsbn(UUID userId, String isbn);

    @Query("SELECT b FROM Book b WHERE b.category = :category AND b.userId = :userId ORDER BY b.createdAt DESC")
    List<Book> findByUserIdAndCategory(UUID userId, String category);

    @Query("SELECT b FROM Book b WHERE b.availableStock > 0 AND b.userId = :userId ORDER BY b.createdAt DESC")
    List<Book> findAvailableBooks(UUID userId);
}