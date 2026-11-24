package org.example.eventsapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String redirect,
            Model model
    ) {
        model.addAttribute("redirect", redirect);
        return "auth/login-form";
    }
}