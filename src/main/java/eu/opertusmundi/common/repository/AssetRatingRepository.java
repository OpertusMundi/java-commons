package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AssetRatingEntity;
import eu.opertusmundi.common.model.rating.AssetAverageRatingDto;
import eu.opertusmundi.common.model.rating.AssetRatingCommandDto;
import eu.opertusmundi.common.model.rating.RatingDto;

@Transactional(readOnly = true)
@Repository
public interface AssetRatingRepository extends JpaRepository<AssetRatingEntity, Integer> {

    @Query("From AssetRating r where r.asset = :asset and r.account = :account")
    Optional<AssetRatingEntity> findOneByAccountIdAndAssetId(String asset, UUID account);

    @Query("From AssetRating r where r.asset = :asset")
    List<AssetRatingEntity> findAllAssetId(@Param("asset") String id);

    @Query("SELECT new eu.opertusmundi.common.model.rating.AssetAverageRatingDto(r.asset, AVG(r.value)) "
         + "FROM AssetRating r "
         + "WHERE r.asset in :pids "
         + "GROUP BY r.asset")
    List<AssetAverageRatingDto> findAllAssetsId(List<String> pids);

    @Transactional(readOnly = false)
    default RatingDto add(AssetRatingCommandDto command) {
        // Create new record or update existing one
        final AssetRatingEntity rating = this
            .findOneByAccountIdAndAssetId(command.getAsset(), command.getAccount())
            .orElse(new AssetRatingEntity());

        rating.setAccount(command.getAccount());
        rating.setAsset(command.getAsset());
        rating.setComment(command.getComment());
        rating.setValue(command.getValue());

        return this.saveAndFlush(rating).toDto();
    }

}
