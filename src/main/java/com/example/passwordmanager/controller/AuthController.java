package com.example.passwordmanager.controller;

import com.example.passwordmanager.model.User;
import com.example.passwordmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Optional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login(Principal principal) {
        if (principal != null)
            return "redirect:/app";
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Principal principal) {
        if (principal != null)
            return "redirect:/app";
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(@RequestParam String username, @RequestParam String password, Model model) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already taken");
            return "signup";
        }
        User u = new User(username, passwordEncoder.encode(password));
        userRepository.save(u);
        return "redirect:/login";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Principal principal,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        Optional<User> maybe = userRepository.findByUsername(principal.getName());
        if (maybe.isEmpty()) {
            model.addAttribute("error", "User not found");
            model.addAttribute("username", principal.getName());
            return "profile";
        }
        User u = maybe.get();
        if (!passwordEncoder.matches(oldPassword, u.getPassword())) {
            model.addAttribute("error", "Current password is incorrect");
            model.addAttribute("username", principal.getName());
            return "profile";
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(u);
        model.addAttribute("success", "Password changed successfully");
        model.addAttribute("username", principal.getName());
        return "profile";
    }
}
