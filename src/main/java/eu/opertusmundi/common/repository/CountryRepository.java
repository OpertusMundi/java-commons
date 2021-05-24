package eu.opertusmundi.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.CountryEntity;

@Repository
@Transactional(readOnly = true)
public interface CountryRepository extends JpaRepository<CountryEntity, Integer> {

}
