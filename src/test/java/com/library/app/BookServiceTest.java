package com.library.app;

import com.library.app.entity.Book;
import com.library.app.repository.BookRepository;
import com.library.app.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void testGetBookById() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Java Programming");
        
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        
        Book found = bookService.getBookById(1L);
        assertEquals("Java Programming", found.getTitle());
    }
}