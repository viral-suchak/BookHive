package com.bookhive.dao;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.stereotype.Repository;

import com.bookhive.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Get all books
    public List<Book> findAll() {
        Query query = entityManager.createQuery("FROM Book");
        return query.getResultList();
    }

    // Get a book by its ID
    public Optional<Book> findById(Long id) {
        Book book = entityManager.find(Book.class, id);
        return Optional.ofNullable(book);
    }

    // Add a new book
    public void save(Book book) {
        entityManager.persist(book);
    }

    // Update an existing book
    public void updateBook(Book book) {
        entityManager.merge(book);
    }

    // Delete a book by its ID
    public void deleteBook(Long id) {
        Book book = entityManager.find(Book.class, id);
        if (book != null) {
            entityManager.remove(book);
        }
    }
}
