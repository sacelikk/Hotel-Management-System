package org.apache.maven.archetypes.hotelflow;

import org.apache.maven.archetypes.hotelflow.Model.*;
import org.apache.maven.archetypes.hotelflow.Service.ReportService;
import org.apache.maven.archetypes.hotelflow.Util.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FR-13: Seasonal Additional Services & Usage Tests")
class SeasonalServiceTest {

    private DataStore dataStore;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        dataStore = DataStore.getInstance();
        dataStore.resetToDefault();
        reportService = new ReportService(dataStore);
    }

    @Test
    @DisplayName("Services with ALL_YEAR are available in any season; seasonal services match configured season")
    void testSeasonalAvailability() {
        Service allYear = new Service(100L, 1L, "All Year Service", "Desc", new BigDecimal("20.00"), Season.ALL_YEAR);
        Service summerOnly = new Service(101L, 1L, "Summer Pool", "Desc", new BigDecimal("15.00"), Season.SUMMER);

        assertTrue(allYear.isAvailableInSeason(Season.WINTER));
        assertTrue(allYear.isAvailableInSeason(Season.SUMMER));
        assertTrue(allYear.isAvailableInSeason(Season.SPRING));
        assertTrue(allYear.isAvailableInSeason(Season.AUTUMN));

        assertTrue(summerOnly.isAvailableInSeason(Season.SUMMER));
        assertFalse(summerOnly.isAvailableInSeason(Season.WINTER));
        assertFalse(summerOnly.isAvailableInSeason(Season.AUTUMN));

        // When deactivated, service must not be available even in-season
        summerOnly.setActive(false);
        assertFalse(summerOnly.isAvailableInSeason(Season.SUMMER));
    }

    @Test
    @DisplayName("Season.forMonth correctly maps calendar months to seasons")
    void testMonthToSeasonMapping() {
        assertEquals(Season.SPRING, Season.forMonth(Month.MARCH));
        assertEquals(Season.SPRING, Season.forMonth(Month.APRIL));
        assertEquals(Season.SUMMER, Season.forMonth(Month.JULY));
        assertEquals(Season.AUTUMN, Season.forMonth(Month.OCTOBER));
        assertEquals(Season.WINTER, Season.forMonth(Month.JANUARY));
    }

    @Test
    @DisplayName("ServiceUsage correctly calculates total cost based on unit price and quantity")
    void testServiceUsagePriceCalculation() {
        BigDecimal unitPrice = new BigDecimal("45.50");
        int quantity = 3;

        ServiceUsage usage = new ServiceUsage(1L, 1L, 101L, quantity, unitPrice);

        assertEquals(new BigDecimal("136.50"), usage.getTotalPrice());
        assertEquals("€136.50", usage.getFormattedTotalPrice());
    }

    @Test
    @DisplayName("Business Rule: Out-of-season service usage is blocked unless explicit override is permitted")
    void testOutOfSeasonUsagePolicy() {
        Season current = Season.getCurrentSeason();
        Season opposite = (current == Season.SUMMER) ? Season.WINTER : Season.SUMMER;

        Service outOfSeasonService = new Service(999L, 1L, "Opposite Season Tour", "Seasonal", new BigDecimal("50.00"), opposite);
        dataStore.addService(outOfSeasonService);

        // Without override -> blocked (returns null)
        ServiceUsage blocked = dataStore.recordServiceUsage(999L, 101L, 1, false);
        assertNull(blocked, "Out-of-season service usage must be blocked per business rules");

        // With authorization override -> accepted
        ServiceUsage authorized = dataStore.recordServiceUsage(999L, 101L, 1, true);
        assertNotNull(authorized, "Authorized override should successfully record the usage");
        assertEquals(999L, authorized.getServiceId());
    }

    @Test
    @DisplayName("In-season and ALL_YEAR services record usage seamlessly in DataStore")
    void testInSeasonUsageRecording() {
        int initialCount = dataStore.getServiceUsages().size();

        // Service 1 is ALL_YEAR Thermal Spa
        ServiceUsage usage = dataStore.recordServiceUsage(1L, 101L, 2, false);

        assertNotNull(usage, "ALL_YEAR service usage should be accepted");
        assertEquals(initialCount + 1, dataStore.getServiceUsages().size());
        assertEquals(new BigDecimal("90.00"), usage.getTotalPrice());
    }

    @Test
    @DisplayName("Acceptance Criteria 8: Seasonal services report aggregates usage counts and revenue")
    void testSeasonalServicesReportGeneration() {
        Report report = reportService.generateSeasonalServicesReport();

        assertNotNull(report);
        assertTrue(report.getTitle().contains("Seasonal Services & Usage Report"));
        assertTrue(report.getContent().contains("SEASONAL ADDITIONAL SERVICES REPORT"));
        assertTrue(report.getContent().contains("Thermal Spa & Massage"));
        assertTrue(report.getContent().contains("Buffet Breakfast Premium"));
        assertTrue(report.getContent().contains("Total Additional Service Revenue"));
    }
}
