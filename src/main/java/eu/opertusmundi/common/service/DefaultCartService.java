package eu.opertusmundi.common.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.order.CartAddCommandDto;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.CartException;
import eu.opertusmundi.common.model.order.CartMessageCode;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationException;

@Service
public class DefaultCartService implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCartService.class);

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private CartStore cartStore;

    @Override
    public CartDto getCart(final UUID cartKey) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        return this.cartStore.getCart(effectiveCartKey);
    }

    @Override
    public CartDto addItem(CartAddCommandDto command) {
        try {
            // Ensure that cart exists; If not create one and update cart key
            final UUID effectiveCartKey = this.ensureCart(command.getCartKey());
            command.setCartKey(effectiveCartKey);

            final CatalogueItemDetailsDto asset = catalogueService.findOne(command.getAssetId());

            // Asset must be available to purchase (provider must be KYC
            // validated)
            if(!asset.isAvailableToPurchase()) {
                throw new CartException(CartMessageCode.PROVIDER_NOT_KYC_VALIDATED, "Asset is not available to purchase");
            }

            // Check asset and pricing model
            final BasePricingModelCommandDto pricingModel = asset.getPricingModels().stream()
                .filter(p -> p.getKey().equals(command.getPricingModelKey()))
                .findFirst()
                .orElse(null);

            if (pricingModel == null) {
                throw new CartException(CartMessageCode.PRICING_MODEL, "Pricing model not found");
            }

            // Compute quotation
            final EffectivePricingModelDto quotation = quotationService.createQuotation(
                asset, command.getPricingModelKey(), command.getParameters(), false
            );

            command.setQuotation(quotation);

            return this.cartStore.addItem(command);
        } catch (final CartException ex) {
            throw ex;
        } catch (final CatalogueServiceException ex) {
            throw new CartException(CartMessageCode.CATALOGUE, "Failed to load asset", ex);
        } catch (final QuotationException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Failed to create cart item", ex);

            throw new CartException(CartMessageCode.ERROR, "Failed to create cart item", ex);
        }
    }

    @Override
    public CartDto removeItem(final UUID cartKey, UUID itemKey) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        try {
            return this.cartStore.removeItem(effectiveCartKey, itemKey);
        } catch (final Exception ex) {
            // Ignore exception
        }

        // TODO: Add error message
        return this.cartStore.getCart(effectiveCartKey);
    }

    @Override
    public CartDto clear(final UUID cartKey) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        return this.cartStore.clear(effectiveCartKey);
    }

    @Override
    public CartDto setAccount(final UUID cartKey, Integer accountId) {
        final UUID effectiveCartKey = this.ensureCart(cartKey);

        return this.cartStore.setAccount(effectiveCartKey, accountId);
    }

    private UUID ensureCart(UUID cartKey) {
        if (cartKey == null) {
            final CartDto cart = this.cartStore.create();

            return cart.getKey();
        }

        return cartKey;
    }

}
