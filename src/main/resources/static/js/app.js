/* Asgasd Digital - interacoes do frontend */
(function () {
    "use strict";

    // Controlos de quantidade (+ / -)
    document.querySelectorAll("[data-quantidade]").forEach(function (controlo) {
        var input = controlo.querySelector("input");
        var minus = controlo.querySelector("[data-menos]");
        var plus = controlo.querySelector("[data-mais]");
        var max = parseInt(input.getAttribute("max") || "99", 10);
        if (minus) {
            minus.addEventListener("click", function () {
                var atual = parseInt(input.value || "1", 10);
                input.value = Math.max(1, atual - 1);
                input.dispatchEvent(new Event("change", { bubbles: true }));
            });
        }
        if (plus) {
            plus.addEventListener("click", function () {
                var atual = parseInt(input.value || "1", 10);
                input.value = Math.min(max, atual + 1);
                input.dispatchEvent(new Event("change", { bubbles: true }));
            });
        }
    });

    // Fecha automaticamente as mensagens de sucesso
    document.querySelectorAll(".alert-auto").forEach(function (alerta) {
        window.setTimeout(function () {
            alerta.classList.add("fade");
            window.setTimeout(function () { alerta.remove(); }, 400);
        }, 5000);
    });

    // Confirmacao em accoes destrutivas
    document.querySelectorAll("form[data-confirmar]").forEach(function (form) {
        form.addEventListener("submit", function (evento) {
            if (!window.confirm(form.getAttribute("data-confirmar"))) {
                evento.preventDefault();
            }
        });
    });

    // Pre-visualizacao da imagem escolhida no painel
    var campoImagem = document.querySelector("[data-preview-imagem]");
    if (campoImagem) {
        campoImagem.addEventListener("change", function () {
            var alvo = document.querySelector("[data-preview-alvo]");
            var ficheiro = campoImagem.files && campoImagem.files[0];
            if (alvo && ficheiro) {
                alvo.src = URL.createObjectURL(ficheiro);
            }
        });
    }

    // Botao voltar ao topo
    var botaoTopo = document.querySelector("[data-topo]");
    if (botaoTopo) {
        window.addEventListener("scroll", function () {
            botaoTopo.classList.toggle("visivel", window.scrollY > 420);
        });
        botaoTopo.addEventListener("click", function () {
            window.scrollTo({ top: 0, behavior: "smooth" });
        });
    }
})();
