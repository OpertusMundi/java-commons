package eu.opertusmundi.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AssetDomainRestrictionEntity;

@Repository
@Transactional(readOnly = true)
public interface AssetDomainRestrictionRepository extends JpaRepository<AssetDomainRestrictionEntity, Integer> {

    @Query("SELECT d FROM AssetDomainRestriction d WHERE d.active = true order by d.name")
    List<AssetDomainRestrictionEntity> findAllActive();

}