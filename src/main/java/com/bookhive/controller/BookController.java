package com.bookhive.controller;

import com.bookhive.model.Book;
import com.bookhive.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "book-list";  // Return a view that lists all books
    }

    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "add-book";  // Return the view to add a new book
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book, Model model) {
        bookService.addBook(book);
        model.addAttribute("message", "Book added successfully!");
        return "redirect:/books";  // Redirect to the books list
    }

    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "edit-book";  // Return the view to edit the book
    }

    @PostMapping("/edit/{id}")
    public String editBook(@PathVariable("id") Long id, @ModelAttribute Book book, Model model) {
        bookService.updateBook(id, book);
        model.addAttribute("message", "Book updated successfully!");
        return "redirect:/books";  // Redirect to the books list
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, Model model) {
        bookService.deleteBook(id);
        model.addAttribute("message", "Book deleted successfully!");
        return "redirect:/books";  // Redirect to the books list
    }
}

