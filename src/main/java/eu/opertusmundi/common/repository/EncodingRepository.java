package eu.opertusmundi.common.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.EncodingEntity;

@Repository
@Transactional(readOnly = true)
public interface EncodingRepository extends JpaRepository<EncodingEntity, Integer> {

    @Override
    Page<EncodingEntity> findAll(Pageable page);

    @Query("SELECT   e FROM Encoding e "
         + "WHERE    (:code is null or e.codeLower like LOWER(CAST(:code as text))) and "
         + "         (e.active = true) "
         + "ORDER BY e.code")
    List<EncodingEntity> findAllActive(String code);

}
