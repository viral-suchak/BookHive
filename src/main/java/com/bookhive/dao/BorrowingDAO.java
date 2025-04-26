package com.bookhive.dao;

import com.bookhive.model.Book;
import com.bookhive.model.Borrowing;
import com.bookhive.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class BorrowingDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(Borrowing borrowing) {
        entityManager.persist(borrowing);
    }

    @Transactional
    public void update(Borrowing borrowing) {
        entityManager.merge(borrowing);
    }

    public Optional<Borrowing> findById(Long id) {
        Borrowing borrowing = entityManager.find(Borrowing.class, id);
        return Optional.ofNullable(borrowing);
    }

    public List<Borrowing> findByUser(User user) {
        return entityManager.createQuery(
                        "SELECT b FROM Borrowing b WHERE b.user = :user ORDER BY b.borrowDate DESC",
                        Borrowing.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Borrowing> findActiveByUser(User user) {
        return entityManager.createQuery(
                        "SELECT b FROM Borrowing b WHERE b.user = :user AND b.status IN ('ACTIVE', 'OVERDUE') ORDER BY b.dueDate",
                        Borrowing.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Borrowing> findOverdueByUser(User user) {
        return entityManager.createQuery(
                        "SELECT b FROM Borrowing b WHERE b.user = :user AND b.status = 'OVERDUE' ORDER BY b.dueDate",
                        Borrowing.class)
                .setParameter("user", user)
                .getResultList();
    }

    public Optional<Borrowing> findActiveByUserAndBook(User user, Book book) {
        try {
            return Optional.of(entityManager.createQuery(
                            "SELECT b FROM Borrowing b WHERE b.user = :user AND b.book = :book AND b.status IN ('ACTIVE', 'OVERDUE')",
                            Borrowing.class)
                    .setParameter("user", user)
                    .setParameter("book", book)
                    .getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Borrowing> findAllOverdue() {
        LocalDate today = LocalDate.now();
        return entityManager.createQuery(
                        "SELECT b FROM Borrowing b WHERE b.status = 'OVERDUE'",
                        Borrowing.class)
                .getResultList();
    }

    @Transactional
    public void updateOverdueStatus() {
        LocalDate today = LocalDate.now();
        entityManager.createQuery(
                        "UPDATE Borrowing b SET b.status = 'OVERDUE' WHERE b.status = 'ACTIVE' AND b.dueDate < :today")
                .setParameter("today", today)
                .executeUpdate();
    }

    public List<Borrowing> findRecentBorrowings(int limit) {
        return entityManager.createQuery(
                        "SELECT b FROM Borrowing b ORDER BY b.borrowDate DESC",
                        Borrowing.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countActiveBorrowingsForUser(User user) {
        return entityManager.createQuery(
                        "SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.status IN ('ACTIVE', 'OVERDUE')",
                        Long.class)
                .setParameter("user", user)
                .getSingleResult();
    }

    public long countActiveBorrowings() {
        return entityManager.createQuery(
                        "SELECT COUNT(b) FROM Borrowing b WHERE b.status = 'ACTIVE'",
                        Long.class)
                .getSingleResult();
    }

    public long countOverdueBorrowings() {
        return entityManager.createQuery(
                        "SELECT COUNT(b) FROM Borrowing b WHERE b.status = 'OVERDUE'",
                        Long.class)
                .getSingleResult();
    }

    public List<Borrowing> findBorrowingHistoryForBook(Book book) {
        return entityManager.createQuery(
                        "SELECT b FROM Borrowing b WHERE b.book = :book ORDER BY b.borrowDate DESC",
                        Borrowing.class)
                .setParameter("book", book)
                .getResultList();
    }

    @Transactional
    public void deleteAllByUser(User user) {
        entityManager.createQuery("DELETE FROM Borrowing b WHERE b.user = :user")
                .setParameter("user", user)
                .executeUpdate();
    }
}