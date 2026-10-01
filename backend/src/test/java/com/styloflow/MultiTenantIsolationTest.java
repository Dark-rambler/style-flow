package com.styloflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Aislamiento entre negocios: lo que crea un negocio no es visible ni utilizable desde otro, y el superadmin solo
 * opera la plataforma. Usa negocios propios (no toca el negocio demo de PosFlowIntegrationTest).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiTenantIsolationTest {

    @Autowired
    MockMvc mvc;

    String superadmin;
    String negocioAId;
    String tokenA;
    String tokenB;
    String clienteA;
    String productoA;
    String ventaA;

    @BeforeAll
    void altaDeNegocios() throws Exception {
        String res = mvc.perform(json(post("/api/plataforma/auth/login"),
                        "{\"username\":\"superadmin\",\"password\":\"superadmin123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        superadmin = JsonPath.read(res, "$.token");

        negocioAId = String.valueOf((Integer) JsonPath.read(crearNegocio("salon-a", true), "$.id"));
        crearNegocio("salon-b", false);
        // Mismo nombre de usuario en ambos negocios
        tokenA = login("salon-a", "admin2");
        tokenB = login("salon-b", "admin2");
    }

    @Test
    @Order(1)
    void altaValidaCodigoUnicoYCatalogoBase() throws Exception {
        mvc.perform(auth(json(post("/api/plataforma/negocios"), negocioJson("salon-a", false)), superadmin))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(auth(get("/api/catalogo/servicios"), tokenA)).andExpect(jsonPath("$.length()").value(11));
        mvc.perform(auth(get("/api/catalogo/servicios"), tokenB)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(2)
    void datosDeUnNegocioNoSonVisiblesDesdeOtro() throws Exception {
        clienteA = id(mvc.perform(auth(json(post("/api/clientes"), "{\"nombre\":\"Cliente de A\"}"), tokenA))
                .andExpect(status().isCreated()));
        productoA = id(mvc.perform(auth(json(post("/api/catalogo/productos"),
                        "{\"nombre\":\"Gel A\",\"sku\":\"GEL-1\",\"precio\":20,\"stock\":5}"), tokenA))
                .andExpect(status().isCreated()));
        // El mismo SKU puede existir en otro negocio
        mvc.perform(auth(json(post("/api/catalogo/productos"),
                        "{\"nombre\":\"Gel B\",\"sku\":\"GEL-1\",\"precio\":25,\"stock\":3}"), tokenB))
                .andExpect(status().isCreated());

        // Ambos negocios pueden tener caja abierta a la vez
        mvc.perform(auth(json(post("/api/caja/abrir"), "{\"montoInicial\":10}"), tokenA)).andExpect(status().isOk());
        mvc.perform(auth(json(post("/api/caja/abrir"), "{\"montoInicial\":20}"), tokenB)).andExpect(status().isOk());

        ventaA = id(mvc.perform(auth(json(post("/api/ventas"), """
                        {"clienteId":%s,"items":[{"tipo":"PRODUCTO","itemId":%s,"cantidad":1}],"metodoPago":"QR"}
                        """.formatted(clienteA, productoA)), tokenA))
                .andExpect(status().isCreated()));

        // Listados de B vacíos de datos de A
        mvc.perform(auth(get("/api/clientes"), tokenB)).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(auth(get("/api/ventas"), tokenB)).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(auth(get("/api/catalogo/productos"), tokenB))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Gel B"));
        mvc.perform(auth(get("/api/usuarios"), tokenB)).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(auth(get("/api/reportes/resumen"), tokenB))
                .andExpect(jsonPath("$.cantidadVentas").value(0))
                .andExpect(jsonPath("$.totalVentas").value(0));
        mvc.perform(auth(get("/api/reportes/resumen"), tokenA)).andExpect(jsonPath("$.cantidadVentas").value(1));
        mvc.perform(auth(get("/api/caja/actual"), tokenB)).andExpect(jsonPath("$.montoInicial").value(20.00));

        // Acceso directo por id a recursos de A desde B
        mvc.perform(auth(get("/api/clientes/" + clienteA), tokenB)).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/ventas/" + ventaA), tokenB)).andExpect(status().isNotFound());
        mvc.perform(auth(json(post("/api/ventas/" + ventaA + "/anular"), "{\"motivo\":\"x\"}"), tokenB))
                .andExpect(status().isNotFound());
        // Vender en B con un producto o cliente de A
        mvc.perform(auth(json(post("/api/ventas"), """
                        {"items":[{"tipo":"PRODUCTO","itemId":%s,"cantidad":1}],"metodoPago":"QR"}
                        """.formatted(productoA)), tokenB))
                .andExpect(status().isNotFound());
        mvc.perform(auth(json(post("/api/ventas"), """
                        {"clienteId":%s,"items":[{"tipo":"PRODUCTO","itemId":%s,"cantidad":1}],"metodoPago":"QR"}
                        """.formatted(clienteA, productoA)), tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    void superadminNoOperaDentroDeUnNegocio() throws Exception {
        mvc.perform(auth(get("/api/ventas"), superadmin)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/plataforma/negocios"), tokenA)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/plataforma/negocios"), superadmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo=='salon-a')].ventas30d").value(1));
    }

    @Test
    @Order(4)
    void negocioSuspendidoNoPuedeOperar() throws Exception {
        mvc.perform(auth(json(put("/api/plataforma/negocios/" + negocioAId + "/estado"), "{\"activo\":false}"), superadmin))
                .andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/clientes"), tokenA)).andExpect(status().isForbidden());
        mvc.perform(json(post("/api/auth/login"), loginJson("salon-a", "admin2"))).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/clientes"), tokenB)).andExpect(status().isOk());

        mvc.perform(auth(json(put("/api/plataforma/negocios/" + negocioAId + "/estado"), "{\"activo\":true}"), superadmin))
                .andExpect(status().isNoContent());
        mvc.perform(auth(get("/api/clientes"), tokenA)).andExpect(status().isOk());
    }

    // ---- helpers ----

    private String crearNegocio(String codigo, boolean catalogo) throws Exception {
        return mvc.perform(auth(json(post("/api/plataforma/negocios"), negocioJson(codigo, catalogo)), superadmin))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }

    private static String negocioJson(String codigo, boolean catalogo) {
        return """
                {"nombre":"Salón %1$s","codigo":"%1$s","adminNombre":"Admin %1$s","adminUsername":"admin2",
                 "adminPassword":"secreto1","catalogoBase":%2$s}
                """.formatted(codigo, catalogo);
    }

    private String login(String negocio, String user) throws Exception {
        String res = mvc.perform(json(post("/api/auth/login"), loginJson(negocio, user)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.token");
    }

    private static String loginJson(String negocio, String user) {
        return "{\"negocio\":\"%s\",\"username\":\"%s\",\"password\":\"secreto1\"}".formatted(negocio, user);
    }

    private static String id(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return String.valueOf((Integer) JsonPath.read(r.andReturn().getResponse().getContentAsString(), "$.id"));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b, String body) {
        return b.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder b, String token) {
        return b.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
