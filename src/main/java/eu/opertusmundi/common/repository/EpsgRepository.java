package eu.opertusmundi.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.EpsgEntity;

@Repository
@Transactional(readOnly = true)
public interface EpsgRepository extends JpaRepository<EpsgEntity, Integer> {

    @Override
    Page<EpsgEntity> findAll(Pageable page);

}
