package com.bookhive.service;

import com.bookhive.dao.BookDAO;
import com.bookhive.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookDAO bookDAO;

    // Get all books
    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }

    // Get book by ID
    public Book getBookById(Long id) {
        Optional<Book> book = bookDAO.findById(id);
        return book.orElse(null); // Return null if not found
    }

    // Add new book
    public void addBook(Book book) {
        bookDAO.save(book);
    }

    // Update existing book
    public void updateBook(Long id, Book updatedBook) {
        Optional<Book> existingBook = bookDAO.findById(id);
        if (existingBook.isPresent()) {
            Book book = existingBook.get();
//            book.setTitle(updatedBook.getTitle());
//            book.setAuthor(updatedBook.getAuthor());
//            book.setGenre(updatedBook.getGenre());
//            book.setPublishedDate(updatedBook.getPublishedDate());
            bookDAO.save(book);  // Save updated book
        }
    }

    // Delete book by ID
    public void deleteBook(Long id) {
        bookDAO.deleteBook(id);
    }
}
