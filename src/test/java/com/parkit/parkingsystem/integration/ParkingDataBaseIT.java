package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig;
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static FareCalculatorService fareCalculatorService;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        dataBaseTestConfig = new DataBaseTestConfig();
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){
        parkingSpotDAO = null;
        ticketDAO = null;
        dataBasePrepareService = null;
        fareCalculatorService = null;
    }

    @Test
    void testParkingACar(){
        // Arrange
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, fareCalculatorService);

        // Act
        parkingService.processIncomingVehicle();

        // Assert
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Le ticket aurait dû être généré.");
        assertNotNull(ticket.getInTime(), "L'heure d'entrée n'a pas été enregistrée.");
        assertNull(ticket.getOutTime(), "L'heure de sortie ne devrait pas être renseignée.");
        assertFalse(ticket.getParkingSpot().isAvailable()
        , "La place de parking aurait dû être marquée comme occupée.");
        assertEquals(ticket.getParkingSpot().getId(), parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR),
                "La place de parking attribuée ne correspond pas à celle attendue.");
        assertTrue(ticketDAO.getNbTickets("ABCDEF") > 0
        , "Le ticket n'a pas été sauvegardé en base.");
    }

    @Test
    void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, fareCalculatorService);
        parkingService.processExitingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket.getOutTime());
        assertTrue(ticket.getParkingSpot().isAvailable());
    }


}
