package com.example.passwordmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

/**
 * Simple controller for non-data pages (landing, about, profile view).
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String landing(Principal principal) {
        if (principal != null)
            return "redirect:/app";
        return "landing";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        if (principal == null)
            return "redirect:/login";
        model.addAttribute("username", principal.getName());
        model.addAttribute("memberSince", "2024");
        return "profile";
    }
}
