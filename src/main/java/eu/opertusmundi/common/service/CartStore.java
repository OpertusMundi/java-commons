package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.order.CartAddCommandDto;
import eu.opertusmundi.common.model.order.CartDto;

public interface CartStore {

    default CartDto create() {
        return this.create(null);
    }

    CartDto create(Integer accountId);

    CartDto getCart(UUID cartKey);

    CartDto addItem(CartAddCommandDto command);

    CartDto removeItem(UUID cartKey, UUID itemKey);

    CartDto clear(UUID cartKey);

    void setAccount(UUID cartKey, Integer accountId);

}
