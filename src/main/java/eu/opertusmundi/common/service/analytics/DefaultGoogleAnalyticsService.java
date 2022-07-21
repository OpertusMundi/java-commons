package eu.opertusmundi.common.service.analytics;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

import eu.opertusmundi.common.model.analytics.DataPoint;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.EnumTemporalUnit;
import eu.opertusmundi.common.model.analytics.GoogleAnalyticsQuery;
import eu.opertusmundi.common.model.analytics.TemporalDimension;

/**
 * Google Analytics client
 *
 * @see https://developers.google.com/analytics/devguides/reporting/core/v4/quickstart/service-java
 */
@Service
@ConditionalOnProperty(name = "opertusmundi.google-analytics.key-file-location")
public class DefaultGoogleAnalyticsService implements GoogleAnalyticsService{

    private static final Logger logger = LoggerFactory.getLogger(DefaultGoogleAnalyticsService.class);

    @Value("${opertusmundi.google-analytics.key-file-location}")
    private String keyFileLocation;

    @Value("${opertusmundi.google-analytics.view-id}")
    private String viewId;

    private AnalyticsReporting service;

    /**
     * Initializes an Analytics Reporting API V4 service object.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @PostConstruct
    private void initializeAnalyticsReporting() throws GeneralSecurityException, IOException {
        final JsonFactory      jsonFactory   = JacksonFactory.getDefaultInstance();
        final HttpTransport    httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final GoogleCredential credential    = GoogleCredential.fromStream(new FileInputStream(keyFileLocation)).createScoped(AnalyticsReportingScopes.all());

        // Construct the Analytics Reporting service object.
        this.service = new AnalyticsReporting.Builder(httpTransport, jsonFactory, credential).build();
    }

    /**
     * Queries the Analytics Reporting API V4.
     *
     * @return GetReportResponse The Analytics Reporting API V4 response.
     * @throws IOException
     */
    private GetReportsResponse getCountOf(GoogleAnalyticsQuery query) throws IOException {
    	
    	final TemporalDimension time = query.getTime();
    	
    	String minDate					=	null;
    	String maxDate					=	null;
    	Dimension groupByYear			= 	null;
    	Dimension groupByMonth			= 	null;
    	Dimension groupByWeek			= 	null;
    	Dimension groupByDay			= 	null;
    	List<Dimension> dimList			=   null;
    	
    	dimList = new ArrayList<Dimension>();
    	dimList.add(new Dimension().setName("ga:pageTitle"));
    	
        if (time != null) {
        	
        	minDate	=	query.getTime().getMin() == null ? "2020-01-01" : query.getTime().getMin().toString();
        	maxDate	=	query.getTime().getMax() == null ? "today" : query.getTime().getMax().toString();
        	
            // Apply temporal dimension grouping
            if (time.getUnit() != null) {
//                result.setTimeUnit(time.getUnit());
            	
                if (time.getUnit().ordinal() >= EnumTemporalUnit.YEAR.ordinal()) {
                	groupByYear = new Dimension().setName("ga:year");
                	dimList.add(groupByYear);
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.MONTH.ordinal()) {
                	groupByMonth = new Dimension().setName("ga:month");
                	dimList.add(groupByMonth);
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.WEEK.ordinal()) {
                	groupByWeek = new Dimension().setName("ga:week");
                	dimList.add(groupByWeek);
                }
                if (time.getUnit().ordinal() >= EnumTemporalUnit.DAY.ordinal()) {
                	groupByDay = new Dimension().setName("ga:day");
                	dimList.add(groupByDay);
                }
            }
        }
    	
    	String expression 	=	null;
    	String alias		=	null;
    	
    	switch(query.getMetric()) {
    	  case COUNT_USERS:
    	    expression	=	"ga:users";
    	    alias		=	"users";
    	    break;
    	  case COUNT_SESSIONS:
    	    expression	=	"ga:sessions";
    	    alias		=	"sessions";
    	    break;
    	  default:
      	    expression	=	"ga:users";
      	    alias		=	"users";
      	    break;
    	}
    	
        // Create the DateRange object.
        final DateRange dateRange = new DateRange();
        dateRange.setStartDate(minDate);
        dateRange.setEndDate(maxDate);

        // Create the Metrics object.
        final Metric sessions = new Metric().setExpression(expression).setAlias(alias);

        // Create the ReportRequest object.
        final ReportRequest request = new ReportRequest()
            .setViewId(this.viewId)
            .setDateRanges(Arrays.asList(dateRange))
            .setMetrics(Arrays.asList(sessions))
            .setDimensions(dimList);

        final ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        final GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);

        // Call the batchGet method.
        final GetReportsResponse response = this.service.reports().batchGet(getReport).execute();

        // Return the response.
        return response;
    }
    
    private void printResponse(GetReportsResponse response) {
        for (final Report report : response.getReports()) {
            final ColumnHeader            header           = report.getColumnHeader();
            final List<String>            dimensionHeaders = header.getDimensions();
            final List<MetricHeaderEntry> metricHeaders    = header.getMetricHeader().getMetricHeaderEntries();
            final List<ReportRow>         rows             = report.getData().getRows();

            if (rows == null) {
                logger.info("No data found for {}", viewId);
                return;
            }

            for (final ReportRow row : rows) {
                final List<String>          dimensions = row.getDimensions();
                final List<DateRangeValues> metrics    = row.getMetrics();

                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    logger.info(dimensionHeaders.get(i) + ": " + dimensions.get(i));
                }

                for (int j = 0; j < metrics.size(); j++) {
                    logger.info("Date Range (" + j + "): ");
                    final DateRangeValues values = metrics.get(j);
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        logger.info(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
                    }
                }
            }
        }
    }
    
    private DataPoint<BigDecimal> mapObjectToDataPoint(GoogleAnalyticsQuery query, ReportRow row, List<String> dimensionHeaders) {

    	final List<String>          dimensions 	= row.getDimensions();
        final List<DateRangeValues> metrics    	= row.getMetrics();    
        final TemporalDimension 	time		= query.getTime();
        DataPoint<BigDecimal> 		p     		= null;
        
        if (dimensionHeaders.get(0).equals("ga:pageTitle") && dimensions.get(0).equals("topio")) {
        	
        	p = new DataPoint<BigDecimal>();
        	
        	final DateRangeValues values = metrics.get(0);
                	
            if (time != null) {
                    	
            	if (time.getUnit() != null) {
            		p.setTime(new DataPoint.TimeInstant());

            		if (time.getUnit().ordinal() >= EnumTemporalUnit.YEAR.ordinal()) {
            			p.getTime().setYear(Integer.parseInt(dimensions.get(1)));
                    }
                    if (time.getUnit().ordinal() >= EnumTemporalUnit.MONTH.ordinal()) {
                    	p.getTime().setMonth(Integer.parseInt(dimensions.get(2)));
                    }
                    if (time.getUnit().ordinal() >= EnumTemporalUnit.WEEK.ordinal()) {
                    	p.getTime().setWeek(Integer.parseInt(dimensions.get(3)));
                    }
                    if (time.getUnit().ordinal() >= EnumTemporalUnit.DAY.ordinal()) {
                    	p.getTime().setDay(Integer.parseInt(dimensions.get(4)));
                    }
            	}
            }
                    
            p.setValue(new BigDecimal(values.getValues().get(0)));
        }
    	return p;
    }
    
    public DataSeries<?> execute(GoogleAnalyticsQuery query) {
    	
    	final DataSeries<BigDecimal> result        = new DataSeries<>();
    	
        try {
        	
            final GetReportsResponse response = getCountOf(query);

            for (final Report report : response.getReports()) {
                final ColumnHeader            header           = report.getColumnHeader();
                final List<String>            dimensionHeaders = header.getDimensions();
                final List<ReportRow>         rows             = report.getData().getRows();

                if (rows == null) {
                    logger.info("No data found for {}", viewId);
                    return null;
                }
                
                for (final ReportRow row : rows) {
                	DataPoint<BigDecimal> datapoint = this.mapObjectToDataPoint(query, row, dimensionHeaders);
                	if (datapoint != null)
                		result.getPoints().add(datapoint);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
		return result;
    }

/*
    
    private GetReportsResponse getReportExample() throws IOException {
        // Create the DateRange object.
        final DateRange dateRange = new DateRange();
        dateRange.setStartDate("7DaysAgo");
        dateRange.setEndDate("today");

        // Create the Metrics object.
        final Metric sessions = new Metric().setExpression("ga:sessions").setAlias("sessions");

        final Dimension pageTitle = new Dimension().setName("ga:pageTitle");

        // Create the ReportRequest object.
        final ReportRequest request = new ReportRequest()
            .setViewId(this.viewId)
            .setDateRanges(Arrays.asList(dateRange))
            .setMetrics(Arrays.asList(sessions))
            .setDimensions(Arrays.asList(pageTitle));

        final ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        final GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);

        // Call the batchGet method.
        final GetReportsResponse response = this.service.reports().batchGet(getReport).execute();

        // Return the response.
        return response;
    }
    
    public void execute() {
        try {
            final GetReportsResponse response = getReportExample();
            printResponse(response);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }


    private void printResponse(GetReportsResponse response) {
        for (final Report report : response.getReports()) {
            final ColumnHeader            header           = report.getColumnHeader();
            final List<String>            dimensionHeaders = header.getDimensions();
            final List<MetricHeaderEntry> metricHeaders    = header.getMetricHeader().getMetricHeaderEntries();
            final List<ReportRow>         rows             = report.getData().getRows();

            if (rows == null) {
                logger.info("No data found for {}", viewId);
                return;
            }

            for (final ReportRow row : rows) {
                final List<String>          dimensions = row.getDimensions();
                final List<DateRangeValues> metrics    = row.getMetrics();

                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    logger.info(dimensionHeaders.get(i) + ": " + dimensions.get(i));
                }

                for (int j = 0; j < metrics.size(); j++) {
                    logger.info("Date Range (" + j + "): ");
                    final DateRangeValues values = metrics.get(j);
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        logger.info(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
                    }
                }
            }
        }
    }
    
*/
    
    
}
