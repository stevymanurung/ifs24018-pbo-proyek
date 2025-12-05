package org.delcom.app.views;

import org.delcom.app.dto.BookForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.BookService;
import org.delcom.app.services.BorrowingService;
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
    private final BorrowingService borrowingService;

    public HomeView(BookService bookService, BorrowingService borrowingService) {
        this.bookService = bookService;
        this.borrowingService = borrowingService;
    }

    @GetMapping
    public String home(@RequestParam(required = false) String search, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // Books
        var books = bookService.getAllBooks(authUser.getId(), search != null ? search : "");
        model.addAttribute("books", books);

        // Statistics
        Long activeBorrowings = borrowingService.countActiveBorrowings(authUser.getId());
        Long returnedBorrowings = borrowingService.countReturnedBorrowings(authUser.getId());
        model.addAttribute("activeBorrowings", activeBorrowings);
        model.addAttribute("returnedBorrowings", returnedBorrowings);
        model.addAttribute("totalBooks", books.size());

        // Book Form
        model.addAttribute("bookForm", new BookForm());

        // Search parameter
        model.addAttribute("search", search);

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}