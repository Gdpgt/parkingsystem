package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    private static ParkingService parkingService;
    
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static FareCalculatorService fareCalculatorService;


    @BeforeEach
    public void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, fareCalculatorService);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    void processExitingVehicleNominalCaseTest() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = createTestTicket(1.5);

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTickets(anyString())).thenReturn(1);
        doNothing().when(fareCalculatorService).calculateFare(any(Ticket.class));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            parkingService.processExitingVehicle();

            verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
            verify(ticketDAO, times(1)).getTicket(any(String.class));
            verify(ticketDAO, times(1)).getNbTickets(any(String.class));
            verify(fareCalculatorService, times(1)).calculateFare(any(Ticket.class));
            verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
            assertEquals(parkingSpot, ticket.getParkingSpot());
            assertTrue(parkingSpot.isAvailable());
            verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

            assertTrue(outContent.toString().contains("Please pay the parking fare: 1.5€"));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void processExitingVehicleWithDiscountCaseTest() throws Exception {
        Ticket ticket = createTestTicket(1.43);

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTickets(anyString())).thenReturn(2);
        doNothing().when(fareCalculatorService).calculateFare(any(Ticket.class), anyBoolean());

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            parkingService.processExitingVehicle();

            verify(fareCalculatorService, times(1)).calculateFare(any(Ticket.class), anyBoolean());

            assertTrue(outContent.toString().contains("Please pay the parking fare (5% discount included): 1.43€"));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void processIncomingVehicleNominalCaseTest() throws Exception {
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.getTicket(anyString())).thenReturn(null);


        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        try {
            parkingService.processIncomingVehicle();

            verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
            verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
        } finally {
            System.setOut(System.out);
        }
    }

    private Ticket createTestTicket(double price) {
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000))); // 1h en arrière
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(price);
        return ticket;
    }   


}
