// package org.delcom.app.views;

// import java.io.IOException;
// import java.nio.file.Path;
// import java.util.UUID;

// import org.delcom.app.dto.CoverBookForm;
// import org.delcom.app.dto.BookForm;
// import org.delcom.app.entities.Book;
// import org.delcom.app.entities.User;
// import org.delcom.app.services.FileStorageService;
// import org.delcom.app.services.BookService;
// import org.delcom.app.utils.ConstUtil;
// import org.springframework.core.io.Resource;
// import org.springframework.core.io.UrlResource;
// // import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.AnonymousAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;


// import jakarta.validation.Valid;

// @Controller
// @RequestMapping("/books")
// public class BookView {

//     private final BookService bookService;
//     private final FileStorageService fileStorageService;

//     public BookView(BookService bookService, FileStorageService fileStorageService) {
//         this.bookService = bookService;
//         this.fileStorageService = fileStorageService;
//     }

//     // Helper method untuk mendapatkan authenticated user
//     private User getAuthenticatedUser() {
//         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//         if (authentication instanceof AnonymousAuthenticationToken) {
//             return null;
//         }
//         Object principal = authentication.getPrincipal();
//         if (principal instanceof User) {
//             return (User) principal;
//         }
//         return null;
//     }

//     @PostMapping("/add")
//     public String postAddBook(@Valid @ModelAttribute("bookForm") BookForm bookForm,
//             RedirectAttributes redirectAttributes) {

//         User authUser = getAuthenticatedUser();
//         if (authUser == null) {
//             return "redirect:/auth/logout";
//         }

//         // Validasi form
//         if (bookForm.getTitle() == null || bookForm.getTitle().isBlank()) {
//             redirectAttributes.addFlashAttribute("error", "Judul buku tidak boleh kosong");
//             redirectAttributes.addFlashAttribute("addBookModalOpen", true);
//             return "redirect:/";
//         }

//         if (bookForm.getAuthor() == null || bookForm.getAuthor().isBlank()) {
//             redirectAttributes.addFlashAttribute("error", "Penulis tidak boleh kosong");
//             redirectAttributes.addFlashAttribute("addBookModalOpen", true);
//             return "redirect:/";
//         }

//         if (bookForm.getIsbn() == null || bookForm.getIsbn().isBlank()) {
//             redirectAttributes.addFlashAttribute("error", "ISBN tidak boleh kosong");
//             redirectAttributes.addFlashAttribute("addBookModalOpen", true);
//             return "redirect:/";
//         }

//         if (bookForm.getCategory() == null || bookForm.getCategory().isBlank()) {
//             redirectAttributes.addFlashAttribute("error", "Kategori tidak boleh kosong");
//             redirectAttributes.addFlashAttribute("addBookModalOpen", true);
//             return "redirect:/";
//         }

//         if (bookForm.getStock() == null || bookForm.getStock() <= 0) {
//             redirectAttributes.addFlashAttribute("error", "Stok harus lebih dari 0");
//             redirectAttributes.addFlashAttribute("addBookModalOpen", true);
//             return "redirect:/";
//         }

//         // Simpan buku
//         try {
//             Book entity = bookService.createBook(
//                     authUser.getId(),
//                     bookForm.getTitle().trim(),
//                     bookForm.getAuthor().trim(),
//                     bookForm.getIsbn().trim(),
//                     bookForm.getCategory().trim(),
//                     bookForm.getPublisher() != null ? bookForm.getPublisher().trim() : null,
//                     bookForm.getPublicationYear(),
//                     bookForm.getStock());

//             if (entity == null) {
//                 redirectAttributes.addFlashAttribute("error", "Gagal menambahkan buku. ISBN mungkin sudah terdaftar.");
//                 redirectAttributes.addFlashAttribute("addBookModalOpen", true);
//                 return "redirect:/";
//             }

//             redirectAttributes.addFlashAttribute("success", "Buku berhasil ditambahkan!");
//             return "redirect:/";
//         } catch (Exception e) {
//             System.err.println("Error adding book: " + e.getMessage());
//             e.printStackTrace();
//             redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan: " + e.getMessage());
//             redirectAttributes.addFlashAttribute("addBookModalOpen", true);
//             return "redirect:/";
//         }
//     }

    

//     @PostMapping("/delete")
//     public String postDeleteBook(@Valid @ModelAttribute("bookForm") BookForm bookForm,
//             RedirectAttributes redirectAttributes) {
        
//         User authUser = getAuthenticatedUser();
//         if (authUser == null) {
//             return "redirect:/auth/logout";
//         }

//         // Validasi form
//         if (bookForm.getId() == null) {
//             redirectAttributes.addFlashAttribute("error", "ID buku tidak valid");
//             return "redirect:/";
//         }

//         // Periksa apakah buku tersedia
//         Book existingBook = bookService.getBookById(authUser.getId(), bookForm.getId());
//         if (existingBook == null) {
//             redirectAttributes.addFlashAttribute("error", "Buku tidak ditemukan");
//             return "redirect:/";
//         }
        
//         // Hapus buku
//         boolean deleted = bookService.deleteBook(authUser.getId(), bookForm.getId());
//         if (!deleted) {
//             redirectAttributes.addFlashAttribute("error", "Gagal menghapus buku");
//             return "redirect:/";
//         }

//         redirectAttributes.addFlashAttribute("success", "Buku berhasil dihapus!");
//         return "redirect:/";
//     }

//     @GetMapping("/{bookId}")
//     public String getDetailBook(@PathVariable UUID bookId, Model model) {
//         User authUser = getAuthenticatedUser();
//         if (authUser == null) {
//             return "redirect:/auth/logout";
//         }
//         model.addAttribute("auth", authUser);

//         // Ambil buku
//         Book book = bookService.getBookById(authUser.getId(), bookId);
//         if (book == null) {
//             return "redirect:/";
//         }
//         model.addAttribute("book", book);

//         // Cover Book Form
//         CoverBookForm coverBookForm = new CoverBookForm();
//         coverBookForm.setId(bookId);
//         model.addAttribute("coverBookForm", coverBookForm);

//         return ConstUtil.TEMPLATE_PAGES_BOOKS_DETAIL;
//     }

//     @PostMapping("/edit-cover")
//     public String postEditCoverBook(@Valid @ModelAttribute("coverBookForm") CoverBookForm coverBookForm,
//             RedirectAttributes redirectAttributes) {

//         User authUser = getAuthenticatedUser();
//         if (authUser == null) {
//             return "redirect:/auth/logout";
//         }

//         if (coverBookForm.getId() == null) {
//             redirectAttributes.addFlashAttribute("error", "ID buku tidak valid");
//             return "redirect:/";
//         }

//         if (coverBookForm.isEmpty()) {
//             redirectAttributes.addFlashAttribute("error", "File cover tidak boleh kosong");
//             redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
//             return "redirect:/books/" + coverBookForm.getId();
//         }

//         // Check if book exists
//         Book book = bookService.getBookById(authUser.getId(), coverBookForm.getId());
//         if (book == null) {
//             redirectAttributes.addFlashAttribute("error", "Buku tidak ditemukan");
//             return "redirect:/";
//         }

//         // Validasi file type
//         if (!coverBookForm.isValidImage()) {
//             redirectAttributes.addFlashAttribute("error", "Format file tidak didukung. Gunakan JPG, PNG, GIF, atau WEBP");
//             redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
//             return "redirect:/books/" + coverBookForm.getId();
//         }

//         // Validasi file size (max 5MB)
//         if (!coverBookForm.isSizeValid(5 * 1024 * 1024)) {
//             redirectAttributes.addFlashAttribute("error", "Ukuran file terlalu besar. Maksimal 5MB");
//             redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
//             return "redirect:/books/" + coverBookForm.getId();
//         }

//         try {
//             // Simpan file
//             String fileName = fileStorageService.storeFile(coverBookForm.getCoverFile(), coverBookForm.getId());

//             // Update buku dengan nama file cover (FIX: tambah userId)
//             bookService.updateCover(authUser.getId(), coverBookForm.getId(), fileName);

//             redirectAttributes.addFlashAttribute("success", "Cover buku berhasil diupload!");
//             return "redirect:/books/" + coverBookForm.getId();
//         } catch (IOException e) {
//             System.err.println("Error uploading cover: " + e.getMessage());
//             e.printStackTrace();
//             redirectAttributes.addFlashAttribute("error", "Gagal mengupload cover: " + e.getMessage());
//             redirectAttributes.addFlashAttribute("editCoverBookModalOpen", true);
//             return "redirect:/books/" + coverBookForm.getId();
//         }
//     }

//     @GetMapping("/cover/{filename:.+}")
//     @ResponseBody
//     public ResponseEntity<Resource> getCoverByFilename(@PathVariable String filename) {
//         try {
//             Path file = fileStorageService.loadFile(filename);
//             Resource resource = new UrlResource(file.toUri());

//             if (resource.exists() && resource.isReadable()) {
//                 // Determine content type
//                 String contentType = "application/octet-stream";
//                 String fileName = filename.toLowerCase();
//                 if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
//                     contentType = "image/jpeg";
//                 } else if (fileName.endsWith(".png")) {
//                     contentType = "image/png";
//                 } else if (fileName.endsWith(".gif")) {
//                     contentType = "image/gif";
//                 } else if (fileName.endsWith(".webp")) {
//                     contentType = "image/webp";
//                 }

//                 return ResponseEntity.ok()
//                         .contentType(MediaType.parseMediaType(contentType))
//                         .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
//                         .body(resource);
//             } else {
//                 return ResponseEntity.notFound().build();
//             }
//         } catch (Exception e) {
//             System.err.println("Error loading cover: " + e.getMessage());
//             return ResponseEntity.notFound().build();
//         }
//     }
// }