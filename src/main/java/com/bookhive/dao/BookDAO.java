package com.bookhive.dao;

import com.bookhive.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(Book book) {
        entityManager.persist(book);
    }

    @Transactional
    public void update(Book book) {
        entityManager.merge(book);
    }

    @Transactional
    public void delete(Book book) {
        entityManager.remove(entityManager.contains(book) ? book : entityManager.merge(book));
    }

    public Optional<Book> findById(Long id) {
        Book book = entityManager.find(Book.class, id);
        return Optional.ofNullable(book);
    }

    public List<Book> findAll() {
        return entityManager.createQuery("SELECT b FROM Book b ORDER BY b.title", Book.class).getResultList();
    }

    public List<Book> findByTitle(String title) {
        return entityManager.createQuery("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:title)", Book.class)
                .setParameter("title", "%" + title + "%")
                .getResultList();
    }

    public List<Book> findByAuthor(String author) {
        return entityManager.createQuery("SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(:author)", Book.class)
                .setParameter("author", "%" + author + "%")
                .getResultList();
    }

    public List<Book> findByGenre(String genre) {
        return entityManager.createQuery("SELECT b FROM Book b WHERE LOWER(b.genre) = LOWER(:genre)", Book.class)
                .setParameter("genre", genre)
                .getResultList();
    }

    public List<Book> findAvailableBooks() {
        return entityManager.createQuery("SELECT b FROM Book b WHERE b.availableCopies > 0", Book.class)
                .getResultList();
    }

    public long countAvailableBooks() {
        return entityManager.createQuery("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > 0", Long.class)
                .getSingleResult();
    }

    public List<Book> search(String query) {
        String searchQuery = "%" + query.toLowerCase() + "%";
        return entityManager.createQuery(
                        "SELECT DISTINCT b FROM Book b WHERE " +
                                "LOWER(b.title) LIKE :query OR " +
                                "LOWER(b.author) LIKE :query OR " +
                                "LOWER(b.isbn) LIKE :query OR " +
                                "LOWER(b.genre) LIKE :query OR " +
                                "LOWER(b.description) LIKE :query",
                        Book.class)
                .setParameter("query", searchQuery)
                .getResultList();
    }

    public List<String> findAllGenres() {
        return entityManager.createQuery(
                "SELECT DISTINCT b.genre FROM Book b WHERE b.genre IS NOT NULL ORDER BY b.genre",
                String.class).getResultList();
    }

    public List<Book> findRecentAdditions(int limit) {
        return entityManager.createQuery("SELECT b FROM Book b ORDER BY b.addedDate DESC", Book.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public boolean existsByIsbn(String isbn) {
        Long count = entityManager.createQuery("SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn", Long.class)
                .setParameter("isbn", isbn)
                .getSingleResult();
        return count > 0;
    }

    public List<Book> findAllPaginated(int page, int pageSize) {
        return entityManager.createQuery("SELECT b FROM Book b ORDER BY b.title", Book.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countBooks() {
        return entityManager.createQuery("SELECT COUNT(b) FROM Book b", Long.class).getSingleResult();
    }
}