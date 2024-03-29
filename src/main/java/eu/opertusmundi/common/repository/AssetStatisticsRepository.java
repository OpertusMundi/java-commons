package eu.opertusmundi.common.repository;

import java.math.BigDecimal;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
    Optional<AssetStatisticsEntity> findAssetStatisticsByPid(String pid);

    @Query("SELECT a FROM AssetStatistics a WHERE a.pid = :pid")
    default Integer[] findAssetSalesAndDownloadsByPid(String pid) {
        final AssetStatisticsEntity e = this.findAssetStatisticsByPid(pid).orElse(null);
        if (e == null) {
            return null;
        }
        return new Integer[]{e.getDownloads(), e.getSales()};
    }

    @Query("SELECT SUM(a.maxPrice) FROM AssetStatistics a WHERE a.active IS true")
    Optional<BigDecimal> findTotalFileAssetValue();

    @Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, SUM(a.maxPrice)) "
            + "FROM AssetStatistics a "
       	 + "WHERE a.active is true "
       	 + "AND to_char(a.publicationDate, 'yyyy-mm-dd') >= :start AND to_char(a.publicationDate, 'yyyy-mm-dd') <= :end "
            + "GROUP BY a.year ORDER BY a.year")
       List<BigDecimalDataPoint> findTotalFileAssetValuePerYear(String start, String end);

       @Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, SUM(a.maxPrice)) "
            + "FROM AssetStatistics a "
       	 + "WHERE a.active is true "
       	 + "AND to_char(a.publicationDate, 'yyyy-mm-dd') >= :start AND to_char(a.publicationDate, 'yyyy-mm-dd') <= :end "
            + "GROUP BY a.year, a.month ORDER BY a.year, a.month")
       List<BigDecimalDataPoint> findTotalFileAssetValuePerMonth(String start, String end);

       @Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, a.week, SUM(a.maxPrice)) "
            + "FROM AssetStatistics a "
       	 + "WHERE a.active is true "
       	 + "AND to_char(a.publicationDate, 'yyyy-mm-dd') >= :start AND to_char(a.publicationDate, 'yyyy-mm-dd') <= :end "
            + "GROUP BY a.year, a.month, a.week ORDER BY a.year, a.month, a.week")
       List<BigDecimalDataPoint> findTotalFileAssetValuePerWeek(String start, String end);

       @Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, a.week, a.day, SUM(a.maxPrice)) "
            + "FROM AssetStatistics a "
       	 + "WHERE a.active is true "
       	 + "AND to_char(a.publicationDate, 'yyyy-mm-dd') >= :start AND to_char(a.publicationDate, 'yyyy-mm-dd') <= :end "
            + "GROUP BY a.year, a.month, a.week, a.day ORDER BY a.year, a.month, a.week, a.day")
       List<BigDecimalDataPoint> findTotalFileAssetValuePerDay(String start, String end);
       
   	@Query("SELECT CAST(COUNT(DISTINCT a.pid) as java.math.BigDecimal) FROM AssetStatistics a WHERE a.active is true")
   	Optional<BigDecimal> countAssets();
   	
   	@Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, CAST(COUNT(DISTINCT a.pid) AS java.math.BigDecimal)) "
   		+  "FROM AssetStatistics a WHERE a.active is true "
   		+  "AND to_char(a.publicationDate, 'yyyy-mm-dd') >= :start AND to_char(a.publicationDate, 'yyyy-mm-dd') <= :end "
   		+  "GROUP BY a.year")
   	List<BigDecimalDataPoint> countAssetsPerYear(String start, String end);
   	
   	@Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, CAST(COUNT(DISTINCT a.pid) AS java.math.BigDecimal)) "
   		+  "FROM AssetStatistics a WHERE a.active is true "
   		+  "AND to_char(a.publicationDate, 'yyyy-mm-dd') >= :start AND to_char(a.publicationDate, 'yyyy-mm-dd') <= :end "
   		+  "GROUP BY a.year, a.month")
   	List<BigDecimalDataPoint> countAssetsPerMonth(String start, String end);
   	
   	@Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, a.week, CAST(COUNT(DISTINCT a.pid) AS java.math.BigDecimal)) "
   			+  "FROM AssetStatistics a WHERE a.active is true "
   			+  "AND to_char(a.publicationDate, 'yyyy-mm-dd') >= :start AND to_char(a.publicationDate, 'yyyy-mm-dd') <= :end "
   			+  "GROUP BY a.year, a.month, a.week")
   	List<BigDecimalDataPoint> countAssetsPerWeek(String start, String end);
   	
   	@Query("SELECT new eu.opertusmundi.common.model.analytics.BigDecimalDataPoint(a.year, a.month, a.week, a.day, CAST(COUNT(DISTINCT a.pid) AS java.math.BigDecimal)) "
   			+  "FROM AssetStatistics a WHERE a.active is true "
   			+  "AND to_char(a.publicationDate, 'yyyy-mm-dd') >= :start AND to_char(a.publicationDate, 'yyyy-mm-dd') <= :end "
   			+  "GROUP BY a.year, a.month, a.week, a.day")
   	List<BigDecimalDataPoint> countAssetsPerDay(String start, String end);

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
    void setStatisticInactive(String pid);

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE AssetStatistics a "
         + "SET a.downloads = a.downloads + 1 "
         + "WHERE a.pid = :pid and a.active = true")
    void increaseDownloads(String pid);

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE AssetStatistics a "
         + "SET a.sales = a.sales + 1 "
         + "WHERE a.pid = :pid and a.active = true")
    void increaseSales(String pid);

    @Modifying
    @Transactional(readOnly = false)
    @Query("DELETE AssetStatistics a WHERE a.pid = :pid")
    int deleteAllByPid(String pid);

}
