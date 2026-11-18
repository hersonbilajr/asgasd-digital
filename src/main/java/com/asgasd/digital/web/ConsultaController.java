package com.asgasd.digital.web;

import com.asgasd.digital.exception.RecursoNaoEncontradoException;
import com.asgasd.digital.service.EncomendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ConsultaController {

    private final EncomendaService encomendas;

    @GetMapping("/consulta")
    public String formulario() {
        return "consulta";
    }

    @PostMapping("/consulta")
    public String consultar(@RequestParam(name = "referencia", required = false) String referencia,
            @RequestParam(name = "email", required = false) String email,
            Model model) {
        model.addAttribute("referencia", referencia);
        model.addAttribute("email", email);
        if (referencia == null || referencia.isBlank() || email == null || email.isBlank()) {
            model.addAttribute("erro", "Preencha a referencia da encomenda e o email usado na compra.");
            return "consulta";
        }
        try {
            model.addAttribute("encomenda", encomendas.consultarPorReferencia(referencia, email));
        } catch (RecursoNaoEncontradoException ex) {
            model.addAttribute("erro", ex.getMessage());
        }
        return "consulta";
    }
}
