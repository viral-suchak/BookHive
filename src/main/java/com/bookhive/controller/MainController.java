package com.bookhive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

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
        return "main";  // This will use main.html template
    }

    // This can be a redirect from the root path
    @GetMapping("/")
    public String home() {
        return "redirect:/main";
    }
}