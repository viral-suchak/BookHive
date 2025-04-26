package com.bookhive.controller;


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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final UserService userService;

    @Autowired
    public AdminController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping
    public String adminDashboard(Model model, HttpSession session) {
        // Check if user is admin
        User.Role userRole = (User.Role) session.getAttribute("userRole");
        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        // Add username for display in UI
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);

        // Add statistics for dashboard
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("recentBooks", bookService.getRecentAdditions(5));
        model.addAttribute("activeBorrowings", bookService.countActiveBorrowings());
        model.addAttribute("overdueBorrowings", bookService.countOverdueBorrowings());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model, HttpSession session) {
        // Check if user is admin
        User.Role userRole = (User.Role) session.getAttribute("userRole");
        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        // Add username for display in UI
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);

        // Get all users from the service
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user is admin
        User.Role userRole = (User.Role) session.getAttribute("userRole");
        if (userRole != User.Role.ADMIN) {
            return "redirect:/main";
        }

        // Check if user is trying to delete themselves
        Long currentUserId = (Long) session.getAttribute("userId");
        if (currentUserId != null && currentUserId.equals(id)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account.");
            return "redirect:/admin/users";
        }
        boolean hasActiveBorrowings = userService.hasActiveBorrowings(id);
        if (hasActiveBorrowings) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete user with active borrowings. User must return all books first.");
            return "redirect:/admin/users";
        }

        boolean success = userService.deleteUser(id);

        if (success) {
            redirectAttributes.addFlashAttribute("message", "User deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user. There may be associated records.");
        }

        return "redirect:/admin/users";
    }

}