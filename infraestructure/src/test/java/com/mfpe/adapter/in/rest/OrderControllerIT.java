package com.mfpe.adapter.in.rest;

import tools.jackson.databind.ObjectMapper;
import com.mfpe.adapter.in.rest.dto.OrderItemResponse;
import com.mfpe.adapter.in.rest.dto.OrderResponse;
import com.mfpe.adapter.in.rest.dto.OrderResponseMapper;
import com.mfpe.exception.OrderAlreadyCancelledException;
import com.mfpe.exception.OrderAlreadyPaidException;
import com.mfpe.exception.OrderNotFoundException;
import com.mfpe.model.entity.Order;
import com.mfpe.model.enums.OrderStatus;
import com.mfpe.model.vo.OrderId;
import com.mfpe.port.in.AddItemToOrderUseCase;
import com.mfpe.port.in.CancelOrderUseCase;
import com.mfpe.port.in.CreateOrderUseCase;
import com.mfpe.port.in.GetOrderByIdUseCase;
import com.mfpe.port.in.PayOrderUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración del context Web.
 *
 * @WebMvcTest levanta únicamente la capa web de Spring:
 * - Registra controllers, filters, exception handlers y mappers de respuesta
 * - Configura MockMvc con serialización JSON real (Jackson)
 * - NO levanta JPA, datasource ni el contexto completo

 * Los use cases se mockean con @MockitoBean porque no pertenecen a la capa web.
 * Así validamos: deserialización JSON → controller → serialización JSON → status HTTP.
 */
@WebMvcTest(controllers = {OrderController.class, GlobalExceptionHandler.class})
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private AddItemToOrderUseCase addItemUseCase;

    @MockitoBean
    private PayOrderUseCase payOrderUseCase;

    @MockitoBean
    private CancelOrderUseCase cancelOrderUseCase;

    @MockitoBean
    private GetOrderByIdUseCase getOrderByIdUseCase;

    @MockitoBean
    private OrderResponseMapper responseMapper;

    // ── Helpers ─────────────────────────────────────────────────────

    private Order buildOrder(String customerId) {
        return Order.reconstitute(
                OrderId.of("550e8400-e29b-41d4-a716-446655440000"),
                customerId, OrderStatus.PENDING, null,
                LocalDateTime.of(2025, 6, 1, 10, 0), List.of());
    }

    private OrderResponse buildOrderResponse(Order order) {
        return new OrderResponse(
                order.getId().toString(),
                order.getCustomerId(),
                order.getStatus(),
                null, null,
                order.getCreatedAt(),
                List.of());
    }

    private OrderResponse buildOrderResponseWithItem() {
        return new OrderResponse(
                "550e8400-e29b-41d4-a716-446655440000",
                "customer-1", OrderStatus.PENDING,
                new BigDecimal("200.00"), "EUR",
                LocalDateTime.of(2025, 6, 1, 10, 0),
                List.of(new OrderItemResponse("PROD-1", "Laptop", 2,
                        new BigDecimal("100.00"), new BigDecimal("200.00"), "EUR")));
    }

    // ── POST /api/orders ─────────────────────────────────────────────

    @Test
    void createOrder_shouldDeserializeJsonBodyCorrectly() throws Exception {
        Order order = buildOrder("customer-json");
        when(createOrderUseCase.createOrder(any())).thenReturn(order);
        when(responseMapper.toResponse(any())).thenReturn(buildOrderResponse(order));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\": \"customer-json\"}"))
                .andExpect(status().isCreated());

        verify(createOrderUseCase).createOrder(
                argThat(cmd -> "customer-json".equals(cmd.customerId())));
    }

    // ── POST /api/orders/{id}/items ──────────────────────────────────

    @Test
    void addItem_shouldReturn200WithUpdatedOrder() throws Exception {
        Order order = buildOrder("customer-1");
        OrderResponse response = buildOrderResponseWithItem();

        when(addItemUseCase.addItem(any())).thenReturn(order);
        when(responseMapper.toResponse(order)).thenReturn(response);

        String body = """
                {
                  "productId":   "PROD-1",
                  "productName": "Laptop",
                  "quantity":    2,
                  "unitPrice":   100.00,
                  "currency":    "EUR"
                }
                """;

        mockMvc.perform(post("/api/orders/550e8400-e29b-41d4-a716-446655440000/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value("PROD-1"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(200.00));
    }

    // ── POST /api/orders/{id}/pay ────────────────────────────────────

    @Test
    void payOrder_shouldReturn204() throws Exception {
        doNothing().when(payOrderUseCase).payOrder("order-123");

        mockMvc.perform(post("/api/orders/order-123/pay"))
                .andExpect(status().isNoContent());

        verify(payOrderUseCase).payOrder("order-123");
    }

    // ── POST /api/orders/{id}/cancel ─────────────────────────────────

    @Test
    void cancelOrder_shouldReturn204() throws Exception {
        doNothing().when(cancelOrderUseCase).cancelOrder("order-456");

        mockMvc.perform(post("/api/orders/order-456/cancel"))
                .andExpect(status().isNoContent());

        verify(cancelOrderUseCase).cancelOrder("order-456");
    }

}
