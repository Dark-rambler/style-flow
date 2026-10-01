package com.styloflow.plataforma;

import com.styloflow.negocio.Negocio;
import com.styloflow.negocio.NegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Estado activo/suspendido de cada negocio, consultado en cada petición (cacheado). */
@Service
@RequiredArgsConstructor
public class NegocioEstadoService {

    static final String CACHE = "negocioActivo";

    private final NegocioRepository negocios;

    @Cacheable(CACHE)
    @Transactional(readOnly = true)
    public boolean activo(Long negocioId) {
        return negocios.findById(negocioId).map(Negocio::isActivo).orElse(false);
    }

    @CacheEvict(cacheNames = CACHE, key = "#negocioId")
    public void invalidar(Long negocioId) {
        // solo invalida la caché
    }
}
