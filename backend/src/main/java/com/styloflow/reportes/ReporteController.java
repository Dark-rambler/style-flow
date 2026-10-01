package com.styloflow.reportes;

import com.styloflow.auth.CurrentUser;
import com.styloflow.reportes.ReporteService.EstilistaTotal;
import com.styloflow.reportes.ReporteService.ItemTop;
import com.styloflow.reportes.ReporteService.Rango;
import com.styloflow.reportes.ReporteService.Resumen;
import com.styloflow.reportes.ReporteService.VentaDia;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService service;
    private final CurrentUser currentUser;

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public Resumen resumen(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.resumen(service.rango(desde, hasta));
    }

    @GetMapping("/ventas-por-dia")
    @PreAuthorize("hasRole('ADMIN')")
    public List<VentaDia> ventasPorDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.ventasPorDia(service.rango(desde, hasta));
    }

    @GetMapping("/estilistas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EstilistaTotal> porEstilista(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.porEstilista(service.rango(desde, hasta), null);
    }

    /** Producción y comisión del estilista autenticado. */
    @GetMapping("/mis-comisiones")
    public List<EstilistaTotal> misComisiones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.porEstilista(service.rango(desde, hasta), currentUser.id());
    }

    @GetMapping("/top")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ItemTop> top(@RequestParam(defaultValue = "SERVICIO") String tipo,
            @RequestParam(defaultValue = "10") int limite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        String t = "PRODUCTO".equalsIgnoreCase(tipo) ? "PRODUCTO" : "SERVICIO";
        return service.topItems(service.rango(desde, hasta), t, limite);
    }

    @GetMapping("/ventas.csv")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportarCsv(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpServletResponse response) throws IOException {
        Rango r = service.rango(desde, hasta);
        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"ventas_" + r.desde() + "_" + r.hasta() + ".csv\"");
        service.exportarVentasCsv(r, response.getWriter());
    }
}
