package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    
    @Query("SELECT b FROM Book b WHERE (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND b.userId = :userId ORDER BY b.createdAt DESC")
    List<Book> findByKeyword(@Param("userId") UUID userId, @Param("keyword") String keyword);

    @Query("SELECT b FROM Book b WHERE b.userId = :userId ORDER BY b.createdAt DESC")
    List<Book> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.userId = :userId")
    Optional<Book> findByUserIdAndId(@Param("userId") UUID userId, @Param("id") UUID id);

    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn AND b.userId = :userId")
    Optional<Book> findByUserIdAndIsbn(@Param("userId") UUID userId, @Param("isbn") String isbn);

    @Query("SELECT b FROM Book b WHERE b.category = :category AND b.userId = :userId ORDER BY b.createdAt DESC")
    List<Book> findByUserIdAndCategory(@Param("userId") UUID userId, @Param("category") String category);

    // Additional query for statistics
    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.userId = :userId")
    List<String> findDistinctCategoriesByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.userId = :userId AND b.category = :category")
    Long countByUserIdAndCategory(@Param("userId") UUID userId, @Param("category") String category);
}