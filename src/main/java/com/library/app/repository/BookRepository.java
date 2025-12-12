package com.library.app.repository;

import com.library.app.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // Fitur Search (Poin 6 & Requirement User)
    List<Book> findByTitleContainingIgnoreCase(String keyword);

    // Untuk Chart Data (Poin 8) - Menghitung buku per kategori
    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category")
    List<Object[]> countBooksByCategory();

    
}
