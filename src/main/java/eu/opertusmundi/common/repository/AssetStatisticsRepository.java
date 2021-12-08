package eu.opertusmundi.common.repository;

import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AssetStatisticsEntity;
import eu.opertusmundi.common.domain.CountryEntity;
import eu.opertusmundi.common.model.analytics.AssetStatisticsCommandDto;
import eu.opertusmundi.common.model.analytics.AssetStatisticsDto;


@Repository
@Transactional(readOnly = true)
public interface AssetStatisticsRepository extends JpaRepository<AssetStatisticsEntity, Integer> {

    @Query("SELECT a FROM AssetStatistics a WHERE a.id = :id")
    Optional<AssetStatisticsEntity> findAssetStatisticsById(Integer id);
    
    @Query("SELECT a FROM AssetStatistics a WHERE a.pid = :pid")
    Page<AssetStatisticsEntity> findAllByPid(String pid, Pageable page);
    
    @Query("SELECT SUM(a.maxPrice) FROM AssetStatistics a "
       	 + "WHERE a.active is true")
    List<AssetStatisticsEntity> findTotalFileAssetValue();
    
    @Query("SELECT a.year, SUM(a.maxPrice) FROM AssetStatistics a "
    	 + "WHERE a.active is true "
         + "GROUP BY a.year")
    List<AssetStatisticsEntity> findTotalFileAssetValuePerYear();
    
    @Query("SELECT a.year, a.month, SUM(a.maxPrice) FROM AssetStatistics a "
    	 + "WHERE a.active is true "
         + "GROUP BY a.year, a.month")
    List<AssetStatisticsEntity> findTotalFileAssetValuePerMonth();
    
    @Query("SELECT a.year, a.month, a.week, SUM(a.maxPrice) FROM AssetStatistics a "
    	 + "WHERE a.active is true "
         + "GROUP BY a.year, a.month, a.week")
    List<AssetStatisticsEntity> findTotalFileAssetValuePerWeek();
    
    @Query("SELECT a.year, a.month, a.week, a.day, SUM(a.maxPrice) FROM AssetStatistics a "
    	 + "WHERE a.active is true "
         + "GROUP BY a.year, a.month, a.week, a.day")
    List<AssetStatisticsEntity> findTotalFileAssetValuePerDay();
    
    @Transactional(readOnly = false)
    default AssetStatisticsDto create(AssetStatisticsCommandDto command) {
        final AssetStatisticsEntity statistics = new AssetStatisticsEntity();
        
        statistics.setPid(command.getPid());
        statistics.setSegment(command.getSegment());
        statistics.setPublicationDate(command.getPublicationDate());
        statistics.setYear(command.getPublicationDate().getYear());
        statistics.setMonth(command.getPublicationDate().getMonthValue());
        statistics.setWeek(command.getPublicationDate().get(WeekFields.of(Locale.getDefault()).weekOfYear()));
        statistics.setDay(command.getPublicationDate().getDayOfMonth());
        statistics.setMaxPrice(command.getMaxPrice());
        statistics.setDownloads(0);
        statistics.setSales(0);
        statistics.setActive(true);
        
        final List<CountryEntity> countries = command.getCountries();
        for (int i = 0 ; i < countries.size() ; i++) {
        	statistics.addCountry(countries.get(i).getCode());
        }

        return this.saveAndFlush(statistics).toDto();
    }
    
    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE AssetStatistics a "
         + "SET a.active = false "
         + "WHERE a.pid = :pid and a.active = true")
    void setStatisticInactive(@Param("pid") String pid);

    @Modifying
    @Transactional(readOnly = false)
    @Query("DELETE AssetStatistics a WHERE a.pid = :pid")
    int deleteAllByPid(String pid);

}