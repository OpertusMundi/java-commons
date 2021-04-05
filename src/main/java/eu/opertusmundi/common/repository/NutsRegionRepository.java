package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.NutsRegionEntity;

@Repository
@Transactional(readOnly = true)
public interface NutsRegionRepository extends JpaRepository<NutsRegionEntity, Integer> {

    @Query("SELECT r FROM NutsRegion r WHERE r.code in :codes")
    List<NutsRegionEntity> findByCode(String[] codes);

    @Query("SELECT r FROM NutsRegion r WHERE r.code = :code")
    Optional<NutsRegionEntity> findByCode(String code);
    
    @Query("SELECT r FROM NutsRegion r WHERE (r.level = :level) and ((r.name like :name) or (r.nameLatin like :name))")
    List<NutsRegionEntity> findAllByNameContainsAndLevel(String name,  Long level);

}
