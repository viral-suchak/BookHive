package com.bookhive.controller;

import com.bookhive.model.Book;
import com.bookhive.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

@Controller
public class MainController {

    private final BookService bookService;

    @Autowired
    public MainController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/main")
    public String mainPage(Model model, HttpSession session) {
        // Check if user is logged in
        String username = (String) session.getAttribute("username");
        if (username == null) {
            // Not logged in, redirect to login
            return "redirect:/users/login";
        }

        // User is logged in, proceed to main page
        model.addAttribute("username", username);

        try {
            // Add featured books (most recent additions)
            List<Book> featured = bookService.getRecentAdditions(4);
            model.addAttribute("featuredBooks", featured);

            model.addAttribute("genres", bookService.getAllGenres());
        } catch (Exception e) {
            model.addAttribute("featuredBooks", Collections.emptyList());
            model.addAttribute("genres", Collections.emptyList());
        }

        return "main";
    }

    // redirect from the root path
    @GetMapping("/")
    public String home() {
        return "redirect:/main";
    }
}