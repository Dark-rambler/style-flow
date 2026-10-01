package com.styloflow.clientes;

import com.styloflow.clientes.ClienteDtos.ClienteRequest;
import com.styloflow.clientes.ClienteDtos.ClienteResponse;
import com.styloflow.common.NotFoundException;
import com.styloflow.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repo;

    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> buscar(String q, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100), Sort.by("nombre"));
        return PageResponse.of(repo.buscar(q == null ? "" : q.trim(), pr), ClienteResponse::from);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return ClienteResponse.from(buscarEntidad(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest req) {
        Cliente c = new Cliente();
        aplicar(c, req);
        return ClienteResponse.from(repo.save(c));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest req) {
        Cliente c = buscarEntidad(id);
        aplicar(c, req);
        return ClienteResponse.from(c);
    }

    public Cliente buscarEntidad(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Cliente", id));
    }

    private static void aplicar(Cliente c, ClienteRequest req) {
        c.setNombre(req.nombre().trim());
        c.setTelefono(blankToNull(req.telefono()));
        c.setEmail(blankToNull(req.email()));
        c.setCiNit(blankToNull(req.ciNit()));
        c.setNotas(blankToNull(req.notas()));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
