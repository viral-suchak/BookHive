package com.bookhive.controller;

import com.bookhive.model.Book;
import com.bookhive.model.Borrowing;
import com.bookhive.model.User;
import com.bookhive.service.BookService;
import com.bookhive.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final UserService userService;
    private static final int PAGE_SIZE = 3; // Number of books per page

    @Autowired
    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping
    public String listBooks(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(required = false) String genre,
                            @RequestParam(required = false) String search,
                            Model model, HttpSession session) {

        // Check if user is logged in
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/users/login";
        }

        List<Book> books;
        long totalBooks;

        // Handle search
        if (search != null && !search.isEmpty()) {
            books = bookService.searchBooks(search);
            totalBooks = books.size();
            model.addAttribute("searchQuery", search);
        }
        // Handle genre filter
        else if (genre != null && !genre.isEmpty()) {
            books = bookService.getBooksByGenre(genre);
            totalBooks = books.size();
            model.addAttribute("selectedGenre", genre);
        }
        // Default pagination
        else {
            books = bookService.getBooksPaginated(page, PAGE_SIZE);
            totalBooks = bookService.getTotalBooks();
        }

        // Get all genres for filter dropdown
        List<String> genres = bookService.getAllGenres();

        // Calculate pagination values
        int totalPages = (int) Math.ceil((double) totalBooks / PAGE_SIZE);

        model.addAttribute("books", books);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("genres", genres);
        model.addAttribute("username", username);

        return "books/list";
    }

    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model, HttpSession session) {
        // Check if user is logged in
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/users/login";
        }

        Optional<Book> optionalBook = bookService.getBookById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            model.addAttribute("book", book);
            model.addAttribute("username", username);

            // Check if the user has already borrowed this book
            User user = userService.findByUsername(username);
            boolean alreadyBorrowed = bookService.getActiveBorrowingsForUser(user).stream()
                    .anyMatch(b -> b.getBook().getId().equals(id));

            model.addAttribute("alreadyBorrowed", alreadyBorrowed);

            return "books/detail";
        } else {
            return "redirect:/books";
        }
    }

    @PostMapping("/{id}/borrow")
    public String borrowBook(@PathVariable Long id, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/users/login";
        }

        // Get user's role
        User.Role userRole = (User.Role) session.getAttribute("userRole");

        // Check if user is admin
        if (userRole == User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error",
                    "Administrators cannot borrow books. Please use a borrower account.");
            return "redirect:/books/" + id;
        }

        User user = userService.findByUsername(username);
        boolean success = bookService.borrowBook(id, user);

        if (success) {
            redirectAttributes.addFlashAttribute("message", "Book borrowed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Unable to borrow the book. It may not be available or you may already have it.");
        }

        return "redirect:/books/" + id;
    }

    @GetMapping("/borrowed")
    public String borrowedBooks(Model model, HttpSession session) {
        // Check if user is logged in
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/users/login";
        }

        User user = userService.findByUsername(username);
        List<Borrowing> activeBorrowings = bookService.getActiveBorrowingsForUser(user);

        model.addAttribute("borrowings", activeBorrowings);
        model.addAttribute("username", username);

        return "books/borrowed";
    }

    @PostMapping("/return/{borrowingId}")
    public String returnBook(@PathVariable Long borrowingId,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/users/login";
        }

        boolean success = bookService.returnBook(borrowingId);

        if (success) {
            redirectAttributes.addFlashAttribute("message", "Book returned successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Unable to return the book.");
        }

        return "redirect:/books/borrowed";
    }

    @GetMapping("/history")
    public String borrowingHistory(Model model, HttpSession session) {
        // Check if user is logged in
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/users/login";
        }

        User user = userService.findByUsername(username);
        List<Borrowing> borrowingHistory = bookService.getBorrowingHistoryForUser(user);

        model.addAttribute("borrowings", borrowingHistory);
        model.addAttribute("username", username);

        return "books/history";
    }

    // Admin functionality - only accessible to ADMIN users
    @GetMapping("/admin/manage")
    public String manageBooks(Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        String username = (String) session.getAttribute("username");
        User.Role userRole = (User.Role) session.getAttribute("userRole");

        if (username == null) {
            return "redirect:/users/login";
        }

        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("username", username);

        return "books/manage";
    }

    @GetMapping("/admin/add")
    public String showAddBookForm(Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        String username = (String) session.getAttribute("username");
        User.Role userRole = (User.Role) session.getAttribute("userRole");

        if (username == null) {
            return "redirect:/users/login";
        }

        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        model.addAttribute("book", new Book());
        model.addAttribute("username", username);

        return "books/add";
    }

    @PostMapping("/admin/add")
    public String addBook(@ModelAttribute Book book,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        // Check if user is logged in and is an admin
        String username = (String) session.getAttribute("username");
        User.Role userRole = (User.Role) session.getAttribute("userRole");

        if (username == null) {
            return "redirect:/users/login";
        }

        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        // Check if ISBN already exists
        if (bookService.existsByIsbn(book.getIsbn())) {
            redirectAttributes.addFlashAttribute("error", "A book with this ISBN already exists.");
            return "redirect:/books/admin/add";
        }

        // Initially all books have same number of total and available copies
        book.setAvailableCopies(book.getTotalCopies());

        bookService.addBook(book);
        redirectAttributes.addFlashAttribute("message", "Book added successfully!");

        return "redirect:/books/admin/manage";
    }

    @GetMapping("/admin/edit/{id}")
    public String showEditBookForm(@PathVariable Long id,
                                   Model model,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is an admin
        String username = (String) session.getAttribute("username");
        User.Role userRole = (User.Role) session.getAttribute("userRole");

        if (username == null) {
            return "redirect:/users/login";
        }

        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        Optional<Book> optionalBook = bookService.getBookById(id);
        if (optionalBook.isPresent()) {
            model.addAttribute("book", optionalBook.get());
            model.addAttribute("username", username);
            return "books/edit";
        } else {
            redirectAttributes.addFlashAttribute("error", "Book not found.");
            return "redirect:/books/admin/manage";
        }
    }

    @PostMapping("/admin/edit/{id}")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute Book book,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        // Check if user is logged in and is an admin
        String username = (String) session.getAttribute("username");
        User.Role userRole = (User.Role) session.getAttribute("userRole");

        if (username == null) {
            return "redirect:/users/login";
        }

        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        // Ensure ID matches
        book.setId(id);

        boolean updated = bookService.updateBook(book);
        if (updated) {
            redirectAttributes.addFlashAttribute("message", "Book updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update book.");
        }

        return "redirect:/books/admin/manage";
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteBook(@PathVariable Long id,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        // Check if user is logged in and is an admin
        String username = (String) session.getAttribute("username");
        User.Role userRole = (User.Role) session.getAttribute("userRole");

        if (username == null) {
            return "redirect:/users/login";
        }

        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        boolean deleted = bookService.deleteBook(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Book deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete book.");
        }

        return "redirect:/books/admin/manage";
    }
}
