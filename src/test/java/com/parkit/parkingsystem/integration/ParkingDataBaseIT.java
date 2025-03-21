package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig;
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static FareCalculatorService fareCalculatorService;
    private static DataBasePrepareService dataBasePrepareService;
    private ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    static void setUp() {
        dataBaseTestConfig = new DataBaseTestConfig();
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    void setUpPerTest() {
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, fareCalculatorService);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    static void tearDown(){
        parkingSpotDAO = null;
        ticketDAO = null;
        dataBasePrepareService = null;
        fareCalculatorService = null;
    }

    @Test
    void testParkingACar(){
        // Act
        parkingService.processIncomingVehicle();

        // Assert
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertFalse(ticket.getParkingSpot().isAvailable());
        assertEquals(ticket.getParkingSpot().getId(), parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR) - 1);
    }

    @Test
    void testParkingLotExit() {
        // Act
        parkingService.processIncomingVehicle();

        // Arrange
        Ticket currentTicket = ticketDAO.getTicket("ABCDEF");
        currentTicket.setInTime(new Date(System.currentTimeMillis() - (60L * 60 * 1000))); // Voiture garée depuis 1h
        ticketDAO.updateExitTicketForTest(currentTicket);

        // Act
        parkingService.processExitingVehicle();

        // Assert
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket.getOutTime());
        assertEquals(1.5, ticket.getPrice(), 0.0001);
        assertEquals(ticket.getParkingSpot().getId(), parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
    }

    @Test
    void testParkingLotExitRecurringUser() {
        // Arrange
        Ticket previousTicket = new Ticket();
        previousTicket.setVehicleRegNumber("ABCDEF");
        previousTicket.setInTime(new Date(System.currentTimeMillis() - (60L * 60 * 1000)));
        previousTicket.setOutTime(new Date());
        previousTicket.setPrice(1.5);
        previousTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticketDAO.saveTicket(previousTicket);

        // Act
        parkingService.processIncomingVehicle();

        // Arrange
        Ticket currentTicket = ticketDAO.getTicket("ABCDEF");
        currentTicket.setInTime(new Date(System.currentTimeMillis() - (60L * 60 * 1000))); // Voiture garée depuis 1h
        ticketDAO.updateExitTicketForTest(currentTicket);

        // Act
        parkingService.processExitingVehicle();

        //Assert
        Ticket finalTicket = ticketDAO.getTicket("ABCDEF");
        assertEquals(1.43, finalTicket.getPrice(), 0.015, "Le prix du ticket avec discount devrait être égal à 1.43€.");
    }
}
