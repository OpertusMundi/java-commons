package eu.opertusmundi.common.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.EpsgEntity;

@Repository
@Transactional(readOnly = true)
public interface EpsgRepository extends JpaRepository<EpsgEntity, Integer> {

    @Override
    Page<EpsgEntity> findAll(Pageable page);

    @Query("SELECT   e FROM Epsg e "
         + "WHERE    (:name is null or e.name like :name) and "
         + "         (:code is null or CAST(e.code as text) like :code) and "
         + "         (e.active = true) "
         + "ORDER BY e.name")
    List<EpsgEntity> findAllActive(String name, String code);

}
