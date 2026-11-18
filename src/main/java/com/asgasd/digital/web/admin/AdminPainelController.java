package com.asgasd.digital.web.admin;

import com.asgasd.digital.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPainelController {

    private final RelatorioService relatorios;

    @GetMapping
    public String painel(Model model) {
        model.addAttribute("indicadores", relatorios.indicadores());
        return "admin/painel";
    }
}
