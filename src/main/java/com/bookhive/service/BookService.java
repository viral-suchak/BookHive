package com.bookhive.service;

import com.bookhive.dao.BookDAO;
import com.bookhive.dao.BorrowingDAO;
import com.bookhive.model.Book;
import com.bookhive.model.Borrowing;
import com.bookhive.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookDAO bookDAO;
    private final BorrowingDAO borrowingDAO;

    @Autowired
    public BookService(BookDAO bookDAO, BorrowingDAO borrowingDAO) {
        this.bookDAO = bookDAO;
        this.borrowingDAO = borrowingDAO;
    }

    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }

    public List<Book> getBooksPaginated(int page, int pageSize) {
        return bookDAO.findAllPaginated(page, pageSize);
    }

    public long getTotalBooks() {
        return bookDAO.countBooks();
    }

    public long countAvailableBooks() {
        return bookDAO.countAvailableBooks();
    }

    public long countActiveBorrowings() {
        return borrowingDAO.countActiveBorrowings();
    }

    public long countOverdueBorrowings() {
        return borrowingDAO.countOverdueBorrowings();
    }

    public Optional<Book> getBookById(Long id) {
        return bookDAO.findById(id);
    }

    public List<Book> searchBooks(String query) {
        return bookDAO.search(query);
    }

    public List<Book> getBooksByGenre(String genre) {
        return bookDAO.findByGenre(genre);
    }

    public List<String> getAllGenres() {
        return bookDAO.findAllGenres();
    }

    public List<Book> getRecentAdditions(int count) {
        return bookDAO.findRecentAdditions(count);
    }

    public List<Borrowing> getRecentBorrowings(int count) {
        return borrowingDAO.findRecentBorrowings(count);
    }

    public List<Borrowing> getAllOverdueBorrowings() {
        return borrowingDAO.findAllOverdue();
    }

    @Transactional
    public void addBook(Book book) {
        bookDAO.save(book);
    }

    @Transactional
    public boolean updateBook(Book book) {
        Optional<Book> existingBook = bookDAO.findById(book.getId());
        if (existingBook.isPresent()) {
            bookDAO.update(book);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deleteBook(Long id) {
        Optional<Book> book = bookDAO.findById(id);
        if (book.isPresent()) {
            bookDAO.delete(book.get());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean borrowBook(Long bookId, User user) {
        Optional<Book> optionalBook = bookDAO.findById(bookId);

        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();

            // Check if the user already has an active borrowing for this book
            if (borrowingDAO.findActiveByUserAndBook(user, book).isPresent()) {
                return false; // User already has this book
            }

            // Check if the book has available copies
            if (book.isAvailable()) {
                // Update book available copies
                book.borrowCopy();
                bookDAO.update(book);

                // Create new borrowing record
                Borrowing borrowing = new Borrowing(user, book);
                borrowingDAO.save(borrowing);

                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean returnBook(Long borrowingId) {
        Optional<Borrowing> optionalBorrowing = borrowingDAO.findById(borrowingId);

        if (optionalBorrowing.isPresent()) {
            Borrowing borrowing = optionalBorrowing.get();

            // Check if the borrowing is active
            if (borrowing.getStatus() == Borrowing.Status.ACTIVE ||
                    borrowing.getStatus() == Borrowing.Status.OVERDUE) {

                // Update borrowing record
                borrowing.returnBook();
                borrowingDAO.update(borrowing);

                // Update book available copies
                Book book = borrowing.getBook();
                book.returnCopy();
                bookDAO.update(book);

                return true;
            }
        }
        return false;
    }

    @Transactional
    public void updateOverdueStatus() {
        borrowingDAO.updateOverdueStatus();
    }

    public List<Borrowing> getActiveBorrowingsForUser(User user) {
        return borrowingDAO.findActiveByUser(user);
    }

    public List<Borrowing> getBorrowingHistoryForUser(User user) {
        return borrowingDAO.findByUser(user);
    }

    public long getActiveBorrowingsCountForUser(User user) {
        return borrowingDAO.countActiveBorrowingsForUser(user);
    }

    public boolean isBookAvailable(Long bookId) {
        Optional<Book> book = bookDAO.findById(bookId);
        return book.isPresent() && book.get().isAvailable();
    }

    public boolean existsByIsbn(String isbn) {
        return bookDAO.existsByIsbn(isbn);
    }
}