package com.asgasd.digital.service;

import com.asgasd.digital.dto.ContactoDto;
import com.asgasd.digital.model.Contacto;
import com.asgasd.digital.model.Usuario;
import com.asgasd.digital.repository.ContactoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactoService {

    private final ContactoRepository contactos;

    @Transactional
    public Contacto registar(ContactoDto dto, Usuario usuario) {
        Contacto contacto = new Contacto();
        contacto.setNome(dto.getNome().trim());
        contacto.setEmail(dto.getEmail().trim().toLowerCase());
        contacto.setTelefone(dto.getTelefone());
        contacto.setAssunto(dto.getAssunto().trim());
        contacto.setMensagem(dto.getMensagem().trim());
        contacto.setUsuario(usuario);
        contacto.setLida(false);
        return contactos.save(contacto);
    }

    @Transactional(readOnly = true)
    public List<Contacto> listar() {
        return contactos.findAllByOrderByCriadoEmDesc();
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas() {
        return contactos.countByLidaFalse();
    }

    @Transactional
    public Contacto alternarLida(Long id) {
        Contacto contacto = contactos.findById(id)
                .orElseThrow(() -> new com.asgasd.digital.exception.RecursoNaoEncontradoException("Mensagem nao encontrada."));
        contacto.setLida(!contacto.isLida());
        return contactos.save(contacto);
    }
}
