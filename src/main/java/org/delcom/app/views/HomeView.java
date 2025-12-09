package org.delcom.app.views;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.delcom.app.dto.BookForm;
import org.delcom.app.entities.Book;
import org.delcom.app.entities.User;
import org.delcom.app.services.BookService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeView {

    private final BookService bookService;

    public HomeView(BookService bookService) {
        this.bookService = bookService;
    }

    // Helper method untuk mendapatkan authenticated user
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    @GetMapping
    public String home(@RequestParam(required = false) String search, 
                      @RequestParam(required = false) Boolean addBookModalOpen,
                      @RequestParam(required = false) UUID editBookModalId,
                      @RequestParam(required = false) Boolean editBookModalOpen,
                      @RequestParam(required = false) UUID deleteBookModalId,
                      @RequestParam(required = false) Boolean deleteBookModalOpen,
                      Model model) {
        
        // Authentication check
        User authUser = getAuthenticatedUser();
        if (authUser == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("auth", authUser);

        // Load books with error handling
        try {
            List<Book> books = bookService.getAllBooks(authUser.getId(), search);
            model.addAttribute("books", books != null ? books : Collections.emptyList());
            model.addAttribute("totalBooks", books != null ? books.size() : 0);
        } catch (Exception e) {
            System.err.println("Error loading books: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("books", Collections.emptyList());
            model.addAttribute("totalBooks", 0);
            model.addAttribute("error", "Gagal memuat daftar buku");
        }

        // Add search term to model
        model.addAttribute("search", search != null ? search : "");

        // Add empty BookForm for modals
        model.addAttribute("bookForm", new BookForm());

        // Handle modal states
        if (Boolean.TRUE.equals(addBookModalOpen)) {
            model.addAttribute("addBookModalOpen", true);
        }
        
        if (Boolean.TRUE.equals(editBookModalOpen) && editBookModalId != null) {
            model.addAttribute("editBookModalOpen", true);
            model.addAttribute("editBookModalId", editBookModalId);
            
            // Load book data for edit modal
            try {
                Book bookToEdit = bookService.getBookById(authUser.getId(), editBookModalId);
                if (bookToEdit != null) {
                    BookForm editForm = new BookForm();
                    editForm.setId(bookToEdit.getId());
                    editForm.setTitle(bookToEdit.getTitle());
                    editForm.setAuthor(bookToEdit.getAuthor());
                    editForm.setIsbn(bookToEdit.getIsbn());
                    editForm.setCategory(bookToEdit.getCategory());
                    editForm.setPublisher(bookToEdit.getPublisher());
                    editForm.setPublicationYear(bookToEdit.getPublicationYear());
                    editForm.setStock(bookToEdit.getStock());
                    model.addAttribute("editBookForm", editForm);
                }
            } catch (Exception e) {
                System.err.println("Error loading book for edit: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (Boolean.TRUE.equals(deleteBookModalOpen) && deleteBookModalId != null) {
            model.addAttribute("deleteBookModalOpen", true);
            model.addAttribute("deleteBookModalId", deleteBookModalId);
            
            // Load book data for delete modal (untuk konfirmasi)
            try {
                Book bookToDelete = bookService.getBookById(authUser.getId(), deleteBookModalId);
                if (bookToDelete != null) {
                    model.addAttribute("deleteBookTitle", bookToDelete.getTitle());
                }
            } catch (Exception e) {
                System.err.println("Error loading book for delete: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}