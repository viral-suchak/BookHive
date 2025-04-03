package com.bookhive.controller;

import com.bookhive.model.User;
import com.bookhive.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model, RedirectAttributes redirectAttributes) {
        boolean success = userService.register(user);

        if (success) {
            redirectAttributes.addFlashAttribute("message", "User registered successfully! Please login.");
            return "redirect:/users/login";
        } else {
            model.addAttribute("error", "Username or email already exists!");
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model,
                        RedirectAttributes redirectAttributes, HttpSession session) {
        if (userService.authenticate(username, password)) {
            // Store user information in session for future use
            session.setAttribute("username", username);
            User user = userService.findByUsername(username);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole());

            // Add welcome message for display on main page
            redirectAttributes.addFlashAttribute("welcomeMessage", "Welcome, " + username + "!");

            return "redirect:/main";  // Redirect to main page
        }
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been successfully logged out.");
        return "redirect:/users/login";
    }

}