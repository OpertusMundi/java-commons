package eu.opertusmundi.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.CountryCapitalCityEntity;
import eu.opertusmundi.common.domain.CountryEntity;
import eu.opertusmundi.common.domain.CountryEuropeEntity;
import eu.opertusmundi.common.domain.LanguageEuropeEntity;

@Repository
@Transactional(readOnly = true)
public interface CountryRepository extends JpaRepository<CountryEntity, Integer> {

    @Query("Select c From Country c order by c.name")
    List<CountryEntity> getCountries();

    @Query("Select c From CountryCapitalCity c order by c.name")
    List<CountryCapitalCityEntity> getCountryCapitalCities();

    @Query("Select c From CountryEurope c order by c.name")
    List<CountryEuropeEntity> getEuropeCountries();

    @Query("Select l From LanguageEurope l where l.active = true order by l.name")
    List<LanguageEuropeEntity> getEuropeLanguages();

}
