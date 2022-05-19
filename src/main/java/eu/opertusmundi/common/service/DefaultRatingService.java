package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.AssetRatingEntity;
import eu.opertusmundi.common.domain.ProviderRatingEntity;
import eu.opertusmundi.common.model.rating.AssetAverageRatingDto;
import eu.opertusmundi.common.model.rating.AssetRatingCommandDto;
import eu.opertusmundi.common.model.rating.ProviderRatingCommandDto;
import eu.opertusmundi.common.model.rating.RatingDto;
import eu.opertusmundi.common.repository.AssetRatingRepository;
import eu.opertusmundi.common.repository.ProviderRatingRepository;

@Service
public class DefaultRatingService implements RatingService {

    @Autowired
    private AssetRatingRepository assetRatingRepository;

    @Autowired
    private ProviderRatingRepository providerRatingRepository;

    @Override
    public List<RatingDto> getAssetRatings(final String id) {
        return this.assetRatingRepository.findAllAssetId(id).stream()
            .map(AssetRatingEntity::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<AssetAverageRatingDto> getAssetsAverageRatings(final List<String> pids) {
        return this.assetRatingRepository.findAllAssetsId(pids);
    }

    @Override
    public List<RatingDto> getProviderRatings(final UUID id) {
        return this.providerRatingRepository.findAllByProviderId(id).stream()
            .map(ProviderRatingEntity::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public void addAssetRating(final AssetRatingCommandDto command) {
        this.assetRatingRepository.add(command);
    }

    @Override
    public void addProviderRating(final ProviderRatingCommandDto command) {
        this.providerRatingRepository.add(command);
    }

}
