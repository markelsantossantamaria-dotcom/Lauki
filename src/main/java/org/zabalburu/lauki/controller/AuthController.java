package org.zabalburu.lauki.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {

        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas o cuenta desactivada");
        }

        if (logout != null) {
            model.addAttribute("logout", "Sesión cerrada con éxito");
        }

        return "login";
    }
}