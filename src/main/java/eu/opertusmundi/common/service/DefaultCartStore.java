package eu.opertusmundi.common.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.opertusmundi.common.domain.CartEntity;
import eu.opertusmundi.common.model.order.CartAddCommandDto;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.repository.CartRepository;

@Component
public class DefaultCartStore implements CartStore {

    @Autowired
    CartRepository cartRepository;

    @Override
    public CartDto create(Integer accountId) {
        return this.cartRepository.create(accountId);
    }

    @Override
    public CartDto getCart(UUID cartKey) {
        final CartEntity cart = this.cartRepository.findOneByKey(cartKey).orElse(null);

        return cart == null ? null : cart.toDto();
    }

    @Override
    public CartDto addItem(CartAddCommandDto command) {
        final CartDto cart = this.cartRepository.addItem(command);

        return cart;
    }

    @Override
    public CartDto removeItem(UUID cartKey, UUID itemKey) {
        final CartDto cart = this.cartRepository.removeItem(cartKey, itemKey);

        return cart;
    }

    @Override
    public CartDto clear(UUID cartKey) {
        final CartDto cart = this.cartRepository.clear(cartKey);

        return cart;
    }

    @Override
    public void setAccount(UUID cartKey, Integer accountId) {
        this.cartRepository.setAccount(cartKey, accountId);
    }

}
