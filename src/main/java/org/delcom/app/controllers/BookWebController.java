package org.delcom.app.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.delcom.app.entities.Book;
import org.delcom.app.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
public class BookWebController {

    @Autowired
    private BookRepository bookRepository;

    // Tentukan folder upload gambar (Sesuaikan dengan konfigurasi Anda sebelumnya jika ada)
    // Biasanya ada di folder 'uploads' atau static resources
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // ------------------------------------------------------------------------
    // 1. FITUR DETAIL (MENGEMBALIKAN HALAMAN DETAIL) - FIX HALAMAN DETAIL
    // ------------------------------------------------------------------------
    @GetMapping("/{id}")
    public String getBookDetail(@PathVariable("id") UUID id, Model model) {
        Book book = bookRepository.findById(id).orElse(null);
        
        if (book == null) {
            return "redirect:/";
        }
        
        model.addAttribute("book", book);
        
        // Sesuaikan path ini dengan lokasi file detail.html Anda
        // Berdasarkan screenshot Anda, sepertinya ada di: templates/pages/books/detail.html
        return "pages/books/detail"; 
    }

    // ... import lainnya

    @PostMapping("/update")
    public String updateBook(@ModelAttribute Book formBook, 
                             // TAMBAHKAN: required = false
                             @RequestParam(value = "cover", required = false) MultipartFile coverFile, 
                             RedirectAttributes redirectAttributes) {
        try {
            // Cari data lama
            Book existingBook = bookRepository.findById(formBook.getId()).orElse(null);
            
            if (existingBook == null) {
                redirectAttributes.addFlashAttribute("error", "Buku tidak ditemukan.");
                return "redirect:/";
            }

            // A. Update Data Teks
            existingBook.setTitle(formBook.getTitle());
            existingBook.setAuthor(formBook.getAuthor());
            existingBook.setIsbn(formBook.getIsbn());
            existingBook.setCategory(formBook.getCategory());
            existingBook.setStock(formBook.getStock());
            existingBook.setPublisher(formBook.getPublisher());
            existingBook.setPublicationYear(formBook.getPublicationYear());
            
            // B. Update Cover (Hanya jika user upload file baru)
            // Cek coverFile != null dulu agar tidak error NullPointerException
            if (coverFile != null && !coverFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(coverFile.getOriginalFilename());
                
                // Tambahkan UUID biar nama file unik
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                
                // Proses Simpan File
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                try {
                    Path filePath = uploadPath.resolve(uniqueFileName);
                    Files.copy(coverFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Update nama file di database
                    existingBook.setCover(uniqueFileName);
                    
                } catch (IOException e) {
                    // Jika gagal upload, catat error tapi jangan hentikan proses save data teks
                    System.err.println("Gagal upload: " + e.getMessage());
                }
            }
            
            // Simpan ke Database
            bookRepository.save(existingBook);
            redirectAttributes.addFlashAttribute("success", "Buku berhasil diperbarui!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal update: " + e.getMessage());
        }

        return "redirect:/"; 
    }

    // ------------------------------------------------------------------------
    // 3. FITUR DELETE
    // ------------------------------------------------------------------------
    @PostMapping("/delete")
    public String deleteBook(@RequestParam("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            if (bookRepository.existsById(id)) {
                bookRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Buku berhasil dihapus!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Buku tidak ditemukan.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus buku.");
        }
        return "redirect:/"; 
    }
}