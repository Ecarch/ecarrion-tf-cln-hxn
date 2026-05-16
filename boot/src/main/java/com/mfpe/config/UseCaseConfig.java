package com.mfpe.config;

import com.mfpe.port.in.AddItemToOrderUseCase;
import com.mfpe.port.in.CancelOrderUseCase;
import com.mfpe.port.in.CreateOrderUseCase;
import com.mfpe.port.in.GetOrderByIdUseCase;
import com.mfpe.port.in.PayOrderUseCase;
import com.mfpe.port.out.FindOrderByIdPort;
import com.mfpe.port.out.InventoryService;
import com.mfpe.port.out.PaymentGateway;
import com.mfpe.port.out.SaveOrderPort;
import com.mfpe.service.AddItemToOrderService;
import com.mfpe.service.CancelOrderService;
import com.mfpe.service.CreateOrderService;
import com.mfpe.service.GetOrderByIdService;
import com.mfpe.service.PayOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateOrderUseCase createOrderUseCase(SaveOrderPort port) {
        return new CreateOrderService(port);
    }

    @Bean
    public AddItemToOrderUseCase addItemToOrderUseCase(FindOrderByIdPort findOrderByIdPort,
                                                       InventoryService inventory,
                                                       SaveOrderPort saveOrderPort) {
        return new AddItemToOrderService(findOrderByIdPort, inventory, saveOrderPort);
    }

    @Bean
    public PayOrderUseCase payOrderUseCase(PaymentGateway paymentGateway,
                                           FindOrderByIdPort findOrderByIdPort,
                                           SaveOrderPort saveOrderPort) {
        return new PayOrderService(paymentGateway, findOrderByIdPort, saveOrderPort);
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(FindOrderByIdPort findOrderByIdPort,
                                                 SaveOrderPort saveOrderPort) {
        return new CancelOrderService(findOrderByIdPort, saveOrderPort);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(FindOrderByIdPort findOrderByIdPort) {
        return new GetOrderByIdService(findOrderByIdPort);
    }
}
