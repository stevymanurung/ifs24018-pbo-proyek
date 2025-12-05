package org.delcom.app.views;

import java.time.LocalDate;
import java.util.UUID;

import org.delcom.app.dto.BorrowingForm;
import org.delcom.app.entities.Book;
import org.delcom.app.entities.Borrowing;
import org.delcom.app.entities.User;
import org.delcom.app.services.BookService;
import org.delcom.app.services.BorrowingService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/borrowings")
public class BorrowingView {

    private final BorrowingService borrowingService;
    private final BookService bookService;

    public BorrowingView(BorrowingService borrowingService, BookService bookService) {
        this.borrowingService = borrowingService;
        this.bookService = bookService;
    }

    @GetMapping
    public String getAllBorrowings(@RequestParam(required = false) String search, Model model) {
        // Autentikasi user
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

        // Borrowings
        var borrowings = borrowingService.getAllBorrowings(authUser.getId(), search != null ? search : "");
        model.addAttribute("borrowings", borrowings);

        // Available books untuk dropdown
        var availableBooks = bookService.getAvailableBooks(authUser.getId());
        model.addAttribute("availableBooks", availableBooks);

        // Borrowing Form
        model.addAttribute("borrowingForm", new BorrowingForm());

        // Search parameter
        model.addAttribute("search", search);

        return ConstUtil.TEMPLATE_PAGES_BORROWINGS_LIST;
    }

    @PostMapping("/add")
    public String postAddBorrowing(@Valid @ModelAttribute("borrowingForm") BorrowingForm borrowingForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;

        // Validasi form
        if (borrowingForm.getBookId() == null) {
            redirectAttributes.addFlashAttribute("error", "Buku harus dipilih");
            redirectAttributes.addFlashAttribute("addBorrowingModalOpen", true);
            return "redirect:/borrowings";
        }

        if (borrowingForm.getBorrowerName() == null || borrowingForm.getBorrowerName().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Nama peminjam tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addBorrowingModalOpen", true);
            return "redirect:/borrowings";
        }

        if (borrowingForm.getBorrowDate() == null) {
            redirectAttributes.addFlashAttribute("error", "Tanggal pinjam tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addBorrowingModalOpen", true);
            return "redirect:/borrowings";
        }

        if (borrowingForm.getDueDate() == null) {
            redirectAttributes.addFlashAttribute("error", "Tanggal jatuh tempo tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addBorrowingModalOpen", true);
            return "redirect:/borrowings";
        }

        // Validasi tanggal
        if (borrowingForm.getDueDate().isBefore(borrowingForm.getBorrowDate())) {
            redirectAttributes.addFlashAttribute("error", "Tanggal jatuh tempo harus setelah tanggal pinjam");
            redirectAttributes.addFlashAttribute("addBorrowingModalOpen", true);
            return "redirect:/borrowings";
        }

        // Simpan peminjaman
        var entity = borrowingService.createBorrowing(
                authUser.getId(),
                borrowingForm.getBookId(),
                borrowingForm.getBorrowerName(),
                borrowingForm.getBorrowerEmail(),
                borrowingForm.getBorrowerPhone(),
                borrowingForm.getBorrowDate(),
                borrowingForm.getDueDate(),
                borrowingForm.getNotes());

        if (entity == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambahkan peminjaman. Buku mungkin tidak tersedia.");
            redirectAttributes.addFlashAttribute("addBorrowingModalOpen", true);
            return "redirect:/borrowings";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Peminjaman berhasil ditambahkan.");
        return "redirect:/borrowings";
    }

    @PostMapping("/edit")
    public String postEditBorrowing(@Valid @ModelAttribute("borrowingForm") BorrowingForm borrowingForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {
        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;

        // Validasi form
        if (borrowingForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID peminjaman tidak valid");
            redirectAttributes.addFlashAttribute("editBorrowingModalOpen", true);
            return "redirect:/borrowings";
        }

        if (borrowingForm.getBorrowerName() == null || borrowingForm.getBorrowerName().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Nama peminjam tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("editBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        if (borrowingForm.getBorrowDate() == null) {
            redirectAttributes.addFlashAttribute("error", "Tanggal pinjam tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("editBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        if (borrowingForm.getDueDate() == null) {
            redirectAttributes.addFlashAttribute("error", "Tanggal jatuh tempo tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("editBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        // Validasi tanggal
        if (borrowingForm.getDueDate().isBefore(borrowingForm.getBorrowDate())) {
            redirectAttributes.addFlashAttribute("error", "Tanggal jatuh tempo harus setelah tanggal pinjam");
            redirectAttributes.addFlashAttribute("editBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("editBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        // Update peminjaman
        var updated = borrowingService.updateBorrowing(
                authUser.getId(),
                borrowingForm.getId(),
                borrowingForm.getBorrowerName(),
                borrowingForm.getBorrowerEmail(),
                borrowingForm.getBorrowerPhone(),
                borrowingForm.getBorrowDate(),
                borrowingForm.getDueDate(),
                borrowingForm.getNotes());

        if (updated == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui peminjaman");
            redirectAttributes.addFlashAttribute("editBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("editBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Peminjaman berhasil diperbarui.");
        return "redirect:/borrowings";
    }

    @PostMapping("/return/{id}")
    public String postReturnBook(@PathVariable UUID id,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;

        // Kembalikan buku
        var returned = borrowingService.returnBook(authUser.getId(), id, LocalDate.now());

        if (returned == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengembalikan buku");
            return "redirect:/borrowings";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Buku berhasil dikembalikan.");
        return "redirect:/borrowings";
    }

    @PostMapping("/delete")
    public String postDeleteBorrowing(@Valid @ModelAttribute("borrowingForm") BorrowingForm borrowingForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;

        // Validasi form
        if (borrowingForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID peminjaman tidak valid");
            redirectAttributes.addFlashAttribute("deleteBorrowingModalOpen", true);
            return "redirect:/borrowings";
        }

        if (borrowingForm.getConfirmBorrowerName() == null || borrowingForm.getConfirmBorrowerName().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi nama peminjam tidak boleh kosong");
            redirectAttributes.addFlashAttribute("deleteBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        // Periksa apakah peminjaman tersedia
        Borrowing existingBorrowing = borrowingService.getBorrowingById(authUser.getId(), borrowingForm.getId());
        if (existingBorrowing == null) {
            redirectAttributes.addFlashAttribute("error", "Peminjaman tidak ditemukan");
            redirectAttributes.addFlashAttribute("deleteBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        if (!existingBorrowing.getBorrowerName().equals(borrowingForm.getConfirmBorrowerName())) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi nama peminjam tidak sesuai");
            redirectAttributes.addFlashAttribute("deleteBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        // Hapus peminjaman
        boolean deleted = borrowingService.deleteBorrowing(
                authUser.getId(),
                borrowingForm.getId());
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus peminjaman");
            redirectAttributes.addFlashAttribute("deleteBorrowingModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteBorrowingModalId", borrowingForm.getId());
            return "redirect:/borrowings";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Peminjaman berhasil dihapus.");
        return "redirect:/borrowings";
    }

    @GetMapping("/{borrowingId}")
    public String getDetailBorrowing(@PathVariable UUID borrowingId, Model model) {
        // Autentikasi user
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

        // Ambil peminjaman
        Borrowing borrowing = borrowingService.getBorrowingById(authUser.getId(), borrowingId);
        if (borrowing == null) {
            return "redirect:/borrowings";
        }
        model.addAttribute("borrowing", borrowing);

        // Ambil detail buku
        Book book = bookService.getBookById(authUser.getId(), borrowing.getBookId());
        model.addAttribute("book", book);

        return ConstUtil.TEMPLATE_PAGES_BORROWINGS_DETAIL;
    }
}