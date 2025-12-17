package com.example.passwordmanager.controller;

import com.example.passwordmanager.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private PasswordService passwordService;

    @ModelAttribute("authenticated")
    public boolean isAuthenticated(Principal principal) {
        return principal != null;
    }

    @ModelAttribute("currentUser")
    public String currentUser(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    @ModelAttribute("entryCount")
    public int entryCount(Principal principal) {
        if (principal == null)
            return 0;
        return passwordService.getAllForUser(principal.getName()).size();
    }

    @ModelAttribute("categoryCount")
    public int categoryCount(Principal principal) {
        if (principal == null)
            return 0;
        List<String> cats = passwordService.getAllForUser(principal.getName()).stream()
                .map(e -> e.getCategory() == null ? "" : e.getCategory()).distinct().toList();
        return cats.size();
    }
}
