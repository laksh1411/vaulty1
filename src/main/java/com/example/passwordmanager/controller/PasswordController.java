package com.example.passwordmanager.controller;

import com.example.passwordmanager.model.PasswordEntry;
import com.example.passwordmanager.service.PasswordService;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Controller
public class PasswordController {

    @Autowired
    private PasswordService service;

    @GetMapping("/app")
    public String appPage(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("entries", service.getAllForUser(principal.getName()));
            model.addAttribute("count", service.getAllForUser(principal.getName()).size());
            model.addAttribute("username", principal.getName());
        } else {
            model.addAttribute("entries", service.getAll());
            model.addAttribute("count", service.getAll().size());
            model.addAttribute("username", "Guest");
        }
        return "app";
    }

    @PostMapping("/add")
    public String add(Principal principal,
            @RequestParam String website,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String category) {
        PasswordEntry e = new PasswordEntry(website, username, password);
        e.setCategory(category);
        if (principal != null) {
            service.saveForUser(e, principal.getName());
        } else {
            service.save(e);
        }
        return "redirect:/app";
    }

    @GetMapping("/delete/{id}")
    public String delete(Principal principal, @PathVariable Long id) {
        if (principal != null)
            service.deleteForUser(id, principal.getName());
        return "redirect:/app";
    }

    // Return decrypted password for a single entry (used by client copy-button)
    @GetMapping("/api/password/{id}")
    @ResponseBody
    public Map<String, String> getPassword(Principal principal, @PathVariable Long id) {
        Map<String, String> resp = new HashMap<>();
        if (principal == null) {
            resp.put("error", "unauthenticated");
            return resp;
        }
        return service.findByIdAndOwner(id, principal.getName())
                .map(e -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("password", service.getDecryptedPassword(e.getId()));
                    return m;
                }).orElseGet(() -> {
                    resp.put("error", "not found");
                    return resp;
                });
    }

    @GetMapping("/api/entry/{id}")
    @ResponseBody
    public ResponseEntity<?> getEntry(Principal principal, @PathVariable Long id) {
        if (principal == null)
            return ResponseEntity.status(401).build();
        return service.findByIdAndOwner(id, principal.getName())
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", e.getId());
                    m.put("website", e.getWebsite());
                    m.put("username", e.getUsername());
                    m.put("category", e.getCategory());
                    m.put("createdAt", e.getCreatedAt());
                    return ResponseEntity.ok(m);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/entry/{id}")
    @ResponseBody
    public ResponseEntity<?> updateEntry(Principal principal, @PathVariable Long id,
            @RequestParam String website,
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String category) {
        if (principal == null)
            return ResponseEntity.status(401).build();
        return service.findByIdAndOwner(id, principal.getName()).map(e -> {
            e.setWebsite(website);
            e.setUsername(username);
            e.setCategory(category);
            if (password != null && !password.isBlank()) {
                e.setPassword(password);
                service.saveEntity(e, true);
            } else {
                service.saveEntity(e, false);
            }
            return ResponseEntity.ok(Map.of("ok", "updated"));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Simple stats endpoint returning counts by category.
    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Integer> stats(Principal principal) {
        List<PasswordEntry> all = (principal == null) ? service.getAll() : service.getAllForUser(principal.getName());
        int social = 0, finance = 0, work = 0, other = 0;
        for (PasswordEntry e : all) {
            String w = (e.getWebsite() == null) ? "" : e.getWebsite().toLowerCase();
            if (w.contains("facebook") || w.contains("twitter") || w.contains("instagram") || w.contains("social")
                    || w.contains("reddit")) {
                social++;
            } else if (w.contains("bank") || w.contains("finance") || w.contains("paypal") || w.contains("stripe")
                    || w.contains("visa") || w.contains("mastercard") || w.contains("credit")) {
                finance++;
            } else if (w.contains("work") || w.contains("atlassian") || w.contains("slack") || w.contains("jira")
                    || w.contains("gitlab") || w.contains("github") || w.contains("git")) {
                work++;
            } else {
                other++;
            }
        }
        Map<String, Integer> m = new HashMap<>();
        m.put("Social", social);
        m.put("Work", work);
        m.put("Finance", finance);
        m.put("Other", other);
        m.put("Total", all.size());
        return m;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();
        List<PasswordEntry> all = service.getAllForUser(principal.getName());
        String csv = "website,username,password,category,createdAt\n" +
                all.stream().map(e -> String.format("%s,%s,%s,%s,%s",
                        escape(e.getWebsite()), escape(e.getUsername()),
                        escape(service.getDecryptedPassword(e.getId())),
                        escape(e.getCategory()), e.getCreatedAt()))
                        .collect(Collectors.joining("\n"));

        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "passwords.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\"\"").replace(",", "\\,");
    }
}
