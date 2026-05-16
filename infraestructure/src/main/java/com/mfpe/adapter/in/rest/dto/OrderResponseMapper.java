package com.mfpe.adapter.in.rest.dto;

import com.mfpe.model.entity.Order;
import com.mfpe.model.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId().toString(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotal() != null ? order.getTotal().amount() : null,
                order.getTotal() != null ? order.getTotal().currency() : null,
                order.getCreatedAt(),
                items
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item){
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice().amount(),
                item.calculateSubtotal().amount(),
                item.getUnitPrice().currency()
        );
    }

}
