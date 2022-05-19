package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.ProviderRatingEntity;
import eu.opertusmundi.common.model.rating.ProviderRatingCommandDto;
import eu.opertusmundi.common.model.rating.RatingDto;

@Transactional(readOnly = true)
@Repository
public interface ProviderRatingRepository extends JpaRepository<ProviderRatingEntity, Integer> {

    @Query("From ProviderRating r where r.provider = :provider and r.account = :account")
    Optional<ProviderRatingEntity> findOneByAccountIdAndAssetId(
        @Param("provider") UUID provider, @Param("account") UUID account
    );

    @Query("From ProviderRating r where r.provider = :provider")
    List<ProviderRatingEntity> findAllByProviderId(@Param("provider") UUID id);

    @Transactional(readOnly = false)
    default RatingDto add(ProviderRatingCommandDto command) {
        // Create new record or update existing one
        final ProviderRatingEntity rating = this
            .findOneByAccountIdAndAssetId(command.getProvider(), command.getAccount())
            .orElse(new ProviderRatingEntity());

        rating.setAccount(command.getAccount());
        rating.setComment(command.getComment());
        rating.setProvider(command.getProvider());
        rating.setValue(command.getValue());

        return this.saveAndFlush(rating).toDto();
    }

}
