package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.order.CartAddCommandDto;
import eu.opertusmundi.common.model.order.CartDto;

public interface CartService {

    default CartDto getCart() {
        return this.getCart(null);
    }

    CartDto getCart(final UUID cartKey);

    CartDto addItem(CartAddCommandDto command);

    CartDto removeItem(final UUID cartKey, UUID itemKey);

    CartDto clear(final UUID cartKey);

    void setAccount(final UUID cartKey, Integer accountId);

}
