package com.styloflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Flujo completo del POS contra un PostgreSQL real (Testcontainers). Los tests comparten estado y van en orden. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PosFlowIntegrationTest {

    @Autowired
    MockMvc mvc;

    /** Estilista creado en el test 3 y reutilizado después. */
    static String estilistaId;

    @Test
    @Order(1)
    void loginInvalidoDevuelve401() throws Exception {
        mvc.perform(json(post("/api/auth/login"), "{\"negocio\":\"demo\",\"username\":\"admin\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/ventas")).andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void ventaSinCajaAbiertaEsRechazada() throws Exception {
        mvc.perform(auth(json(post("/api/ventas"),
                        "{\"items\":[{\"tipo\":\"SERVICIO\",\"itemId\":1,\"cantidad\":1}],\"metodoPago\":\"QR\"}"),
                        admin()))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @Order(3)
    void ventaCalculaTotalesCambioEIvaYDescuentaStock() throws Exception {
        String token = admin();
        estilistaId = crearEstilista(token, "luis", 50);
        mvc.perform(auth(json(post("/api/caja/abrir"), "{\"montoInicial\":100}"), token))
                .andExpect(status().isOk());

        // Servicio a precio manual 100 + 2 productos (seed: SHA-500 a 85) = 270; descuento 20 -> 250
        String productoId = idProducto(token, "SHA-500");
        String body = """
                {"items":[
                  {"tipo":"SERVICIO","itemId":1,"cantidad":1,"precioUnitario":100,"estilistaId":%s},
                  {"tipo":"PRODUCTO","itemId":%s,"cantidad":2}],
                 "descuento":20,"metodoPago":"EFECTIVO","montoRecibido":300}
                """.formatted(estilistaId, productoId);
        mvc.perform(auth(json(post("/api/ventas"), body), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(270.00))
                .andExpect(jsonPath("$.total").value(250.00))
                .andExpect(jsonPath("$.cambio").value(50.00))
                .andExpect(jsonPath("$.iva").value(28.76)); // 250 * 13 / 113

        String productos = mvc.perform(auth(get("/api/catalogo/productos"), token)).andReturn().getResponse()
                .getContentAsString();
        Integer stock = JsonPath.<java.util.List<Integer>>read(productos, "$[?(@.sku=='SHA-500')].stock").get(0);
        assertThat(stock).isEqualTo(18);

        // Comisión: 100 * (250/270) = 92.59 -> 50% = 46.30
        mvc.perform(auth(get("/api/reportes/estilistas"), token))
                .andExpect(jsonPath("$[0].total").value(92.59))
                .andExpect(jsonPath("$[0].comision").value(46.30));
    }

    @Test
    @Order(4)
    void estilistaNoPuedeVenderNiVerUsuarios() throws Exception {
        String token = login("luis", "secreto1");
        mvc.perform(auth(json(post("/api/ventas"),
                        "{\"items\":[{\"tipo\":\"SERVICIO\",\"itemId\":1,\"cantidad\":1}],\"metodoPago\":\"QR\"}"), token))
                .andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/usuarios"), token)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/reportes/mis-comisiones"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comision").value(46.30));
    }

    @Test
    @Order(5)
    void cierreDeCajaCalculaDiferencia() throws Exception {
        // Esperado: 100 inicial + 250 en efectivo = 350; contado 340 -> -10
        mvc.perform(auth(json(post("/api/caja/cerrar"), "{\"efectivoContado\":340}"), admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"))
                .andExpect(jsonPath("$.efectivoEsperado").value(350.00))
                .andExpect(jsonPath("$.diferencia").value(-10.00))
                .andExpect(jsonPath("$.totalVentas").value(250.00));

        mvc.perform(auth(get("/api/reportes/resumen"), admin()))
                .andExpect(jsonPath("$.cantidadVentas").value(1))
                .andExpect(jsonPath("$.totalVentas").value(250.00));
    }

    @Test
    @Order(6)
    void cortesiaEnProductoNoReduceLaComisionDelServicio() throws Exception {
        String token = admin();
        mvc.perform(auth(json(post("/api/caja/abrir"), "{\"montoInicial\":0}"), token)).andExpect(status().isOk());
        String productoId = idProducto(token, "SHA-500");

        // Descuento de línea mayor al importe -> 422
        mvc.perform(auth(json(post("/api/ventas"), """
                        {"items":[{"tipo":"PRODUCTO","itemId":%s,"cantidad":1,"descuento":999}],"metodoPago":"QR"}
                        """.formatted(productoId)), token))
                .andExpect(status().isUnprocessableContent());

        // Servicio 100 con estilista + producto 85 en cortesía: total 100
        mvc.perform(auth(json(post("/api/ventas"), """
                        {"items":[
                          {"tipo":"SERVICIO","itemId":1,"cantidad":1,"precioUnitario":100,"estilistaId":%s},
                          {"tipo":"PRODUCTO","itemId":%s,"cantidad":1,"descuento":85}],
                         "metodoPago":"QR"}
                        """.formatted(estilistaId, productoId)), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(100.00))
                .andExpect(jsonPath("$.descuento").value(0))
                .andExpect(jsonPath("$.total").value(100.00))
                .andExpect(jsonPath("$.items[1].descuento").value(85.00))
                .andExpect(jsonPath("$.items[1].subtotal").value(0));

        // Producción del día: 92.59 (venta del test 3) + 100 completos de esta venta -> 192.59; 50% = 96.30
        mvc.perform(auth(get("/api/reportes/estilistas"), token))
                .andExpect(jsonPath("$[0].total").value(192.59))
                .andExpect(jsonPath("$[0].comision").value(96.30));

        mvc.perform(auth(get("/api/ventas"), token))
                .andExpect(jsonPath("$.content[0].cajaAbierta").value(true))
                .andExpect(jsonPath("$.content[1].cajaAbierta").value(false));
    }

    // ---- helpers ----

    private String admin() throws Exception {
        return login("admin", "admin123");
    }

    private String login(String user, String pass) throws Exception {
        String res = mvc.perform(json(post("/api/auth/login"),
                        "{\"negocio\":\"demo\",\"username\":\"%s\",\"password\":\"%s\"}".formatted(user, pass)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.token");
    }

    private String crearEstilista(String token, String username, int comision) throws Exception {
        String res = mvc.perform(auth(json(post("/api/usuarios"), """
                        {"nombre":"Estilista %1$s","username":"%1$s","password":"secreto1","rol":"ESTILISTA",
                         "comisionPorcentaje":%2$d}
                        """.formatted(username, comision)), token))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return String.valueOf((Integer) JsonPath.read(res, "$.id"));
    }

    private String idProducto(String token, String sku) throws Exception {
        String res = mvc.perform(auth(get("/api/catalogo/productos"), token)).andReturn().getResponse()
                .getContentAsString();
        return String.valueOf(JsonPath.<java.util.List<Integer>>read(res, "$[?(@.sku=='" + sku + "')].id").get(0));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b, String body) {
        return b.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder b, String token) {
        return b.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
