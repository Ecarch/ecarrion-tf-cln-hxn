package com.mfpe.port.in;

import com.mfpe.command.AddItemToOrderCommand;
import com.mfpe.model.entity.Order;

/*
 * Interfaz que define el caso de uso para agregar un item a una orden existente.
 */
public interface AddItemToOrderUseCase {
    Order addItem(AddItemToOrderCommand command);
}