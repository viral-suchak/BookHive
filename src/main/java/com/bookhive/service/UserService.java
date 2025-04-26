package com.bookhive.service;

import com.bookhive.dao.BorrowingDAO;
import com.bookhive.dao.UserDAO;
import com.bookhive.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;
    private final BorrowingDAO borrowingDAO;

    @Autowired
    public UserService(UserDAO userDAO, BorrowingDAO borrowingDAO) {
        this.userDAO = userDAO;
        this.borrowingDAO = borrowingDAO;
    }

    @Transactional
    public boolean register(User user) {
        // Check if username or email already exists
        if (userDAO.existsByUsername(user.getUsername()) ||
                userDAO.existsByEmail(user.getEmail())) {
            return false;
        }

        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole(User.Role.BORROWER);
        }

        // Save the user to the database
        userDAO.save(user);
        return true;
    }

    public boolean authenticate(String username, String password) {
        User user = userDAO.findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }

    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public boolean hasActiveBorrowings(Long userId) {
        Optional<User> optionalUser = userDAO.findById(userId);
        if (!optionalUser.isPresent()) {
            return false;
        }

        User user = optionalUser.get();
        long count = borrowingDAO.countActiveBorrowingsForUser(user);
        return count > 0;
    }

    @Transactional
    public boolean deleteUser(Long userId) {
        // Check if user exists
        Optional<User> optionalUser = userDAO.findById(userId);
        if (!optionalUser.isPresent()) {
            return false;
        }

        User user = optionalUser.get();

        // Check for ACTIVE borrowings - this is the critical check
        long activeCount = borrowingDAO.countActiveBorrowingsForUser(user);
        if (activeCount > 0) {
            // Don't delete users with active borrowings
            return false;
        }

        // For users without active borrowings, you can either:
        // 1. Delete their borrowing history and then delete the user
        borrowingDAO.deleteAllByUser(user);

        // 2. Or keep their borrowing history by anonymizing the user
        // This would require a different approach

        // Then delete the user
        return userDAO.deleteById(userId);
    }
}