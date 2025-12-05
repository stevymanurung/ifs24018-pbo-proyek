package org.delcom.app.views;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.delcom.app.dto.CoverBookForm;
import org.delcom.app.dto.BookForm;
import org.delcom.app.entities.Book;
import org.delcom.app.entities.Borrowing;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.BookService;
import org.delcom.app.services.BorrowingService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/books")
public class BookView {

    private final BookService bookService;
    private final BorrowingService borrowingService;
    private final FileStorageService fileStorageService;

    public BookView(BookService bookService, BorrowingService borrowingService, FileStorageService fileStorageService) {
        this.bookService = bookService;
        this.borrowingService = borrowingService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/add")
    public String postAddBook(@Valid @ModelAttribute("bookForm") BookForm bookForm,
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
        if (bookForm.getTitle() == null || bookForm.getTitle().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Judul buku tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addBookModalOpen", true);
            return "redirect:/";
        }

        if (bookForm.getAuthor() == null || bookForm.getAuthor().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Penulis tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addBookModalOpen", true);
            return "redirect:/";
        }

        if (bookForm.getIsbn() == null || bookForm.getIsbn().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "ISBN tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addBookModalOpen", true);
            return "redirect:/";
        }

        if (bookForm.getCategory() == null || bookForm.getCategory().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Kategori tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addBookModalOpen", true);
            return "redirect:/";
        }

        if (bookForm.getStock() == null || bookForm.getStock() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Stok harus lebih dari 0");
            redirectAttributes.addFlashAttribute("addBookModalOpen", true);
            return "redirect:/";
        }

        // Simpan buku
        var entity = bookService.createBook(
                authUser.getId(),
                bookForm.getTitle(),
                bookForm.getAuthor(),
                bookForm.getIsbn(),
                bookForm.getCategory(),
                bookForm.getPublisher(),
                bookForm.getPublicationYear(),
                bookForm.getStock());

        if (entity == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambahkan buku. ISBN mungkin sudah terdaftar.");
            redirectAttributes.addFlashAttribute("addBookModalOpen", true);
            return "redirect:/";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Buku berhasil ditambahkan.");
        return "redirect:/";
    }

    @PostMapping("/edit")
    public String postEditBook(@Valid @ModelAttribute("bookForm") BookForm bookForm,
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
        if (bookForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID buku tidak valid");
            redirectAttributes.addFlashAttribute("editBookModalOpen", true);
            return "redirect:/";
        }

        if (bookForm.getTitle() == null || bookForm.getTitle().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Judul buku tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editBookModalOpen", true);
            redirectAttributes.addFlashAttribute("editBookModalId", bookForm.getId());
            return "redirect:/";
        }

        if (bookForm.getAuthor() == null || bookForm.getAuthor().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Penulis tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editBookModalOpen", true);
            redirectAttributes.addFlashAttribute("editBookModalId", bookForm.getId());
            return "redirect:/";
        }

        if (bookForm.getIsbn() == null || bookForm.getIsbn().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "ISBN tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editBookModalOpen", true);
            redirectAttributes.addFlashAttribute("editBookModalId", bookForm.getId());
            return "redirect:/";
        }

        if (bookForm.getCategory() == null || bookForm.getCategory().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Kategori tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editBookModalOpen", true);
            redirectAttributes.addFlashAttribute("editBookModalId", bookForm.getId());
            return "redirect:/";
        }

        if (bookForm.getStock() == null || bookForm.getStock() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Stok harus lebih dari 0");
            redirectAttributes.addFlashAttribute("editBookModalOpen", true);
            redirectAttributes.addFlashAttribute("editBookModalId", bookForm.getId());
            return "redirect:/";
        }

        // Update buku
        var updated = bookService.updateBook(
                authUser.getId(),
                bookForm.getId(),
                bookForm.getTitle(),
                bookForm.getAuthor(),
                bookForm.getIsbn(),
                bookForm.getCategory(),
                bookForm.getPublisher(),
                bookForm.getPublicationYear(),
                bookForm.getStock());

        if (updated == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui buku. ISBN mungkin sudah digunakan.");
            redirectAttributes.addFlashAttribute("editBookModalOpen", true);
            redirectAttributes.addFlashAttribute("editBookModalId", bookForm.getId());
            return "redirect:/";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Buku berhasil diperbarui.");
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String postDeleteBook(@Valid @ModelAttribute("bookForm") BookForm bookForm,
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
        if (bookForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID buku tidak valid");
            redirectAttributes.addFlashAttribute("deleteBookModalOpen", true);
            return "redirect:/";
        }

        if (bookForm.getConfirmTitle() == null || bookForm.getConfirmTitle().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi judul tidak boleh kosong");
            redirectAttributes.addFlashAttribute("deleteBookModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteBookModalId", bookForm.getId());
            return "redirect:/";
        }

        // Periksa apakah buku tersedia
        Book existingBook = bookService.getBookById(authUser.getId(), bookForm.getId());
        if (existingBook == null) {
            redirectAttributes.addFlashAttribute("error", "Buku tidak ditemukan");
            redirectAttributes.addFlashAttribute("deleteBookModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteBookModalId", bookForm.getId());
            return "redirect:/";
        }

        if (!existingBook.getTitle().equals(bookForm.getConfirmTitle())) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi judul tidak sesuai");
            redirectAttributes.addFlashAttribute("deleteBookModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteBookModalId", bookForm.getId());
            return "redirect:/";
        }

        // Hapus buku
        boolean deleted = bookService.deleteBook(
                authUser.getId(),
                bookForm.getId());
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus buku");
            redirectAttributes.addFlashAttribute("deleteBookModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteBookModalId", bookForm.getId());
            return "redirect:/";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Buku berhasil dihapus.");
        return "redirect:/";
    }

    @GetMapping("/{bookId}")
    public String getDetailBook(@PathVariable UUID bookId, Model model) {
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

        // Ambil buku
        Book book = bookService.getBookById(authUser.getId(), bookId);
        if (book == null) {
            return "redirect:/";
        }
        model.addAttribute("book", book);

        // Ambil riwayat peminjaman buku ini
        List<Borrowing> borrowings = borrowingService.getBorrowingsByBookId(authUser.getId(), bookId);
        model.addAttribute("borrowings", borrowings);

        // Cover Book Form
        CoverBookForm coverBookForm = new CoverBookForm();
        coverBookForm.setId(bookId);
        model.addAttribute("coverBookForm", coverBookForm);

        return ConstUtil.TEMPLATE_PAGES_BOOKS_DETAIL;
    }

    @PostMapping("/edit-cover")
    public String postEditCoverBook(@Valid @ModelAttribute("coverBookForm") CoverBookForm coverBookForm,
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

        if (coverBookForm.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "File cover tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
            return "redirect:/books/" + coverBookForm.getId();
        }

        // Check if book exists
        Book book = bookService.getBookById(authUser.getId(), coverBookForm.getId());
        if (book == null) {
            redirectAttributes.addFlashAttribute("error", "Buku tidak ditemukan");
            redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
            return "redirect:/";
        }

        // Validasi manual file type
        if (!coverBookForm.isValidImage()) {
            redirectAttributes.addFlashAttribute("error", "Format file tidak didukung. Gunakan JPG, PNG, GIF, atau WEBP");
            redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
            return "redirect:/books/" + coverBookForm.getId();
        }

        // Validasi file size (max 5MB)
        if (!coverBookForm.isSizeValid(5 * 1024 * 1024)) {
            redirectAttributes.addFlashAttribute("error", "Ukuran file terlalu besar. Maksimal 5MB");
            redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
            return "redirect:/books/" + coverBookForm.getId();
        }

        try {
            // Simpan file
            String fileName = fileStorageService.storeFile(coverBookForm.getCoverFile(), coverBookForm.getId());

            // Update buku dengan nama file cover
            bookService.updateCover(coverBookForm.getId(), fileName);

            redirectAttributes.addFlashAttribute("success", "Cover buku berhasil diupload");
            return "redirect:/books/" + coverBookForm.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengupload cover");
            redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
            return "redirect:/books/" + coverBookForm.getId();
        }
    }

    @GetMapping("/cover/{filename:.+}")
    @ResponseBody
    public Resource getCoverByFilename(@PathVariable String filename) {
        try {
            Path file = fileStorageService.loadFile(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}