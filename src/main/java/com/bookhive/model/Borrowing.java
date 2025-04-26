package com.bookhive.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "borrowings")
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ACTIVE, RETURNED, OVERDUE
    }

    // Default constructor
    public Borrowing() {
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(14); // Default loan period - 2 weeks
        this.status = Status.ACTIVE;
    }

    // Constructor with user and book
    public Borrowing(User user, Book book) {
        this();
        this.user = user;
        this.book = book;
    }

    // Constructor with all fields
    public Borrowing(User user, Book book, LocalDate borrowDate, LocalDate dueDate) {
        this.user = user;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = Status.ACTIVE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Business methods
    public void returnBook() {
        this.returnDate = LocalDate.now();
        this.status = Status.RETURNED;
    }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && status == Status.ACTIVE;
    }

    public void updateStatus() {
        if (status == Status.ACTIVE && isOverdue()) {
            status = Status.OVERDUE;
        }
    }

    public int getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    @Override
    public String toString() {
        return "Borrowing{" +
                "id=" + id +
                ", user=" + user.getUsername() +
                ", book=" + book.getTitle() +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                '}';
    }
}