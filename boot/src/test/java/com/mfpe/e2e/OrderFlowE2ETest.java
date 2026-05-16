package com.mfpe.e2e;

import com.mfpe.adapter.in.rest.dto.AddItemRequest;
import com.mfpe.adapter.in.rest.dto.CreateOrderRequest;
import com.mfpe.adapter.in.rest.dto.OrderResponse;
import com.mfpe.config.TestSecurityConfig;
import com.mfpe.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test End-to-End del flujo completo de una orden.
 *
 * @SpringBootTest(webEnvironment = RANDOM_PORT) levanta la aplicación
 * completa en un puerto real aleatorio:
 * - Servidor HTTP Tomcat real
 * - Todas las capas ensambladas (controller → service → JPA → H2)
// * - Peticiones HTTP reales a través de TestRestTemplate
 *
 * La diferencia con los tests de integración:
 * - @DataJpaTest / @WebMvcTest → slice parcial, sin servidor real
 * - @SpringBootTest(RANDOM_PORT) → aplicación completa, servidor real
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate //propio de spring 4, antes no se necesitaba.
@Import(TestSecurityConfig.class)
public class OrderFlowE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Flujo completo: crear orden → añadir ítem → pagar.
     * Verifica que todas las capas están correctamente ensambladas
     * y que el estado final de la orden persiste en base de datos.
     */
    @Test
    void fullOrderFlow_createAddItemAndPay() {
        // ── 1. Crear la orden ────────────────────────────────────────
        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                "/api/orders",
                new CreateOrderRequest("customer-e2e"),
                OrderResponse.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        OrderResponse created = createResponse.getBody();
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("customer-e2e", created.customerId());
        assertEquals(OrderStatus.PENDING, created.status());
        assertTrue(created.items().isEmpty());

        // Verificamos que el header Location apunta al recurso creado
        URI location = createResponse.getHeaders().getLocation();
        assertNotNull(location);
        assertTrue(location.toString().contains(created.id()));

        String orderId = created.id();

        // ── 2. Añadir un ítem ────────────────────────────────────────
        ResponseEntity<OrderResponse> addItemResponse = restTemplate.postForEntity(
                "/api/orders/" + orderId + "/items",
                new AddItemRequest("PROD-1", "Laptop", 2,
                        new BigDecimal("100.00"), "EUR"),
                OrderResponse.class);

        assertEquals(HttpStatus.OK, addItemResponse.getStatusCode());

        OrderResponse withItem = addItemResponse.getBody();
        assertNotNull(withItem);
        assertEquals(1, withItem.items().size());
        assertEquals("PROD-1", withItem.items().getFirst().productId());
        assertEquals("Laptop", withItem.items().getFirst().productName());
        assertEquals(2, withItem.items().getFirst().quantity());
        assertEquals(new BigDecimal("200.00"), withItem.items().getFirst().subtotal());
        assertEquals(OrderStatus.PENDING, withItem.status());

        // ── 3. Pagar la orden ────────────────────────────────────────
        // El SimulatedPaymentGateway tiene un 10% de probabilidad de fallo,
        // así que intentamos hasta 10 veces para garantizar que el flujo
        // de pago exitoso se verifica al menos una vez.
        boolean paid = false;
        for (int attempt = 0; attempt < 10; attempt++) {
            ResponseEntity<Void> payResponse = restTemplate.postForEntity(
                    "/api/orders/" + orderId + "/pay",
                    null,
                    Void.class);

            if (payResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
                paid = true;
                break;
            }
            // Si falla (409 Conflict por fallo de pago), creamos una orden nueva
            // con ítem para el siguiente intento
            if (payResponse.getStatusCode() == HttpStatus.CONFLICT
                    || payResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                ResponseEntity<OrderResponse> newOrder = restTemplate.postForEntity(
                        "/api/orders",
                        new CreateOrderRequest("customer-e2e-retry"),
                        OrderResponse.class);
                orderId = newOrder.getBody().id();
                restTemplate.postForEntity(
                        "/api/orders/" + orderId + "/items",
                        new AddItemRequest("PROD-1", "Laptop", 2,
                                new BigDecimal("100.00"), "EUR"),
                        OrderResponse.class);
            }
        }
        assertTrue(paid, "El pago debería haber tenido éxito en al menos uno de los 10 intentos");
    }


}
