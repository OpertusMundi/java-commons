package eu.opertusmundi.common.repository.contract;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.ProviderTemplateContractEntity;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;

@Repository
@Transactional(readOnly = true)
public interface ProviderTemplateContractRepository extends JpaRepository<ProviderTemplateContractEntity, Integer> {

    @Query("SELECT c FROM ProviderContract c WHERE c.owner.id = :providerId and c.key = :contractKey")
    Optional<ProviderTemplateContractEntity> findOneByKey(Integer providerId, UUID contractKey);

    @Query("SELECT c FROM ProviderContract c WHERE c.owner.key = :providerKey and c.key = :contractKey")
    Optional<ProviderTemplateContractEntity> findOneByKey(UUID providerKey, UUID contractKey);

    @Query("SELECT c FROM ProviderContract c INNER JOIN c.parent p WHERE c.owner.key = :providerKey ORDER BY c.defaultContract DESC")
    Page<ProviderTemplateContractEntity> findAll(UUID providerKey, Pageable pageable);

    default Page<ProviderTemplateContractDto> findAllObjects(UUID providerKey, Pageable pageable) {
        return this.findAll(providerKey, pageable).map(c -> c.toDto(false));
    }

    default Optional<ProviderTemplateContractDto> findOneObject(Integer providerId, UUID contractKey) {
        return this.findOneByKey(providerId, contractKey).map(c -> c.toDto(true));
    }

}
