package com.asgasd.digital.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErroController implements ErrorController {

    @RequestMapping("/error")
    public String erro(HttpServletRequest request, Model model) {
        Object atributo = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int codigo = atributo == null ? 500 : Integer.parseInt(atributo.toString());
        String titulo = switch (codigo) {
            case 403 -> "Acesso negado";
            case 404 -> "Pagina nao encontrada";
            case 400 -> "Pedido invalido";
            default -> "Ocorreu um erro";
        };
        String mensagem = switch (codigo) {
            case 403 -> "Nao tem permissoes para acessar esta area.";
            case 404 -> "O endereco pedido nao existe ou foi removido.";
            case 400 -> "Os dados enviados nao sao validos.";
            default -> "Estamos a resolver o problema. Tente novamente dentro de alguns instantes.";
        };
        model.addAttribute("codigo", codigo);
        model.addAttribute("titulo", titulo);
        model.addAttribute("mensagem", mensagem);
        return "erro";
    }
}
