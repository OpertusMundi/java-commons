package eu.opertusmundi.common.repository;

import java.math.BigDecimal;
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
import eu.opertusmundi.common.model.analytics.AssetStatisticsCommandDto;
import eu.opertusmundi.common.model.analytics.AssetStatisticsDto;
import eu.opertusmundi.common.model.analytics.BigDecimalDataPoint;
import eu.opertusmundi.common.model.spatial.CountryEuropeDto;


@Repository
@Transactional(readOnly = true)
public interface AssetStatisticsRepository extends JpaRepository<AssetStatisticsEntity, Integer> {

    @Query("SELECT a FROM AssetStatistics a WHERE a.id = :id")
    Optional<AssetStatisticsEntity> findAssetStatisticsById(Integer id);

    @Query("SELECT a FROM AssetStatistics a WHERE a.pid = :pid")
    Page<AssetStatisticsEntity> findAllByPid(String pid, Pageable page);

    @Query("SELECT SUM(a.maxPrice) FROM AssetStatistics a "
       	 + "WHERE a.active is true")
    Optional<BigDecimal> findTotalFileAssetValue();

    @Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, SUM(a.maxPrice)) "
         + "FROM AssetStatistics a "
    	 + "WHERE a.active is true "
         + "GROUP BY a.year")
    List<BigDecimalDataPoint> findTotalFileAssetValuePerYear();

    @Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, SUM(a.maxPrice)) "
         + "FROM AssetStatistics a "
    	 + "WHERE a.active is true "
         + "GROUP BY a.year, a.month")
    List<BigDecimalDataPoint> findTotalFileAssetValuePerMonth();

    @Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, a.week, SUM(a.maxPrice)) "
         + "FROM AssetStatistics a "
    	 + "WHERE a.active is true "
         + "GROUP BY a.year, a.month, a.week")
    List<BigDecimalDataPoint> findTotalFileAssetValuePerWeek();

    @Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, a.week, a.day, SUM(a.maxPrice)) "
         + "FROM AssetStatistics a "
    	 + "WHERE a.active is true "
         + "GROUP BY a.year, a.month, a.week, a.day")
    List<BigDecimalDataPoint> findTotalFileAssetValuePerDay();

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

        for (final CountryEuropeDto c : command.getCountries()) {
            statistics.addCountry(c.getCode());
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
