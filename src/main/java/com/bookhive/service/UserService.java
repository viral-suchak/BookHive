package com.bookhive.service;

import com.bookhive.dao.UserDAO;
import com.bookhive.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
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
}