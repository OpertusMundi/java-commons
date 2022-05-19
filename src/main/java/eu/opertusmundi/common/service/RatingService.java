package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.rating.AssetAverageRatingDto;
import eu.opertusmundi.common.model.rating.AssetRatingCommandDto;
import eu.opertusmundi.common.model.rating.ProviderRatingCommandDto;
import eu.opertusmundi.common.model.rating.RatingDto;

public interface RatingService {

    List<RatingDto> getAssetRatings(final String id);

    List<AssetAverageRatingDto> getAssetsAverageRatings(final List<String> pids);

    List<RatingDto> getProviderRatings(final UUID id);

    void addAssetRating(final AssetRatingCommandDto command);

    void addProviderRating(final ProviderRatingCommandDto command);

}
