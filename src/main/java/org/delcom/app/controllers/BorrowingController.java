package org.delcom.app.controllers;

import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Borrowing;
import org.delcom.app.entities.User;
import org.delcom.app.services.BookService;
import org.delcom.app.services.BorrowingService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/borrowings") 
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final BookService bookService;

    @Autowired
    protected AuthContext authContext;

    public BorrowingController(BorrowingService borrowingService, BookService bookService) {
        this.borrowingService = borrowingService;
        this.bookService = bookService;
    }

    // ========================================================================
    // VIEW ENDPOINTS (Mengembalikan Tampilan HTML)
    // ========================================================================

    @GetMapping
    public String listBorrowings(Model model,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) boolean addBorrowingModalOpen,
                                 @RequestParam(required = false) boolean editBorrowingModalOpen,
                                 @RequestParam(required = false) boolean deleteBorrowingModalOpen,
                                 @RequestParam(required = false) UUID editBorrowingModalId,
                                 @RequestParam(required = false) UUID deleteBorrowingModalId) {

        if (!authContext.isAuthenticated()) return "redirect:/" + ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        User authUser = authContext.getAuthUser();

        // Mengirim data peminjaman ke HTML
        model.addAttribute("borrowings", borrowingService.getAllBorrowings(authUser.getId(), search));
        model.addAttribute("search", search);
        model.addAttribute("auth", authUser);

        // Logic Modal Tambah (Perlu data buku untuk Dropdown)
        if (addBorrowingModalOpen) {
            model.addAttribute("addBorrowingModalOpen", true);
            model.addAttribute("borrowingForm", new Borrowing());
            model.addAttribute("availableBooks", bookService.getAvailableBooks(authUser.getId()));
        }

        // Logic Modal Edit
        if (editBorrowingModalOpen && editBorrowingModalId != null) {
            Borrowing borrowing = borrowingService.getBorrowingById(authUser.getId(), editBorrowingModalId);
            if (borrowing != null) {
                model.addAttribute("editBorrowingModalOpen", true);
                model.addAttribute("editBorrowingModalId", editBorrowingModalId);
                model.addAttribute("borrowingForm", borrowing);
            }
        }

        // Logic Modal Hapus
        if (deleteBorrowingModalOpen && deleteBorrowingModalId != null) {
            model.addAttribute("deleteBorrowingModalOpen", true);
            model.addAttribute("deleteBorrowingModalId", deleteBorrowingModalId);
            model.addAttribute("borrowingForm", new Borrowing());
        }

        return ConstUtil.TEMPLATE_PAGES_BORROWINGS_LIST;
    }

    @GetMapping("/{id}")
    public String borrowingDetail(@PathVariable UUID id, Model model) {
        if (!authContext.isAuthenticated()) return "redirect:/" + ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        
        Borrowing borrowing = borrowingService.getBorrowingById(authContext.getAuthUser().getId(), id);
        if (borrowing == null) return "redirect:/borrowings";

        model.addAttribute("borrowing", borrowing);
        return ConstUtil.TEMPLATE_PAGES_BORROWINGS_DETAIL;
    }

    // CREATE
    @PostMapping("/add")
    public String createBorrowing(@ModelAttribute Borrowing reqBorrowing, RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) return "redirect:/login";

        Borrowing newBorrowing = borrowingService.createBorrowing(
                authContext.getAuthUser().getId(),
                reqBorrowing.getBookId(),
                reqBorrowing.getBorrowerName(),
                reqBorrowing.getBorrowerEmail(),
                reqBorrowing.getBorrowerPhone(),
                reqBorrowing.getBorrowDate(),
                reqBorrowing.getDueDate(),
                reqBorrowing.getNotes());

        if (newBorrowing == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menyimpan. Pastikan stok buku tersedia.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Peminjaman berhasil dicatat.");
        }
        return "redirect:/borrowings";
    }

    // UPDATE
    @PostMapping("/edit")
    public String updateBorrowing(@ModelAttribute Borrowing reqBorrowing, RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) return "redirect:/login";

        borrowingService.updateBorrowing(
                authContext.getAuthUser().getId(),
                reqBorrowing.getId(),
                reqBorrowing.getBorrowerName(),
                reqBorrowing.getBorrowerEmail(),
                reqBorrowing.getBorrowerPhone(),
                reqBorrowing.getBorrowDate(),
                reqBorrowing.getDueDate(),
                reqBorrowing.getNotes());

        redirectAttributes.addFlashAttribute("success", "Data peminjaman diperbarui.");
        return "redirect:/borrowings";
    }

    // DELETE
    @PostMapping("/delete")
    public String deleteBorrowing(@ModelAttribute Borrowing reqBorrowing, RedirectAttributes redirectAttributes) {
        if (!authContext.isAuthenticated()) return "redirect:/login";

        borrowingService.deleteBorrowing(authContext.getAuthUser().getId(), reqBorrowing.getId());
        redirectAttributes.addFlashAttribute("success", "Data peminjaman dihapus.");
        return "redirect:/borrowings";
    }

    // ========================================================================
    // API ENDPOINTS (JSON) - Wajib untuk Javascript di Modal (delete.html)
    // ========================================================================

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Borrowing>>> getBorrowingByIdApi(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) return ResponseEntity.status(403).build();

        Borrowing borrowing = borrowingService.getBorrowingById(authContext.getAuthUser().getId(), id);
        return ResponseEntity.ok(new ApiResponse<>("success", "Data fetched", Map.of("borrowing", borrowing)));
    }
}