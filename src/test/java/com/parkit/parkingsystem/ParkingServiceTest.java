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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;
    @Mock
    private FareCalculatorService fareCalculatorService;

    private String vehicleRegNumber;
    private ArgumentCaptor<ParkingSpot> parkingSpotCaptor;
    private ArgumentCaptor<Ticket> ticketCaptor;

    @BeforeEach
    public void setUpPerTest() {
        try {
            vehicleRegNumber = "ABCDEF";
            parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
            ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, fareCalculatorService);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }


    @Test
    void processExitingVehicleNominalCaseTest() throws Exception {
        // Arrange
        Ticket ticket = createTestTicket();
        when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTickets(vehicleRegNumber)).thenReturn(1);

        doAnswer(invocation -> {
            final Ticket capturedTicket = invocation.getArgument(0);
            capturedTicket.setPrice(1.5);
            return null;
        }).when(fareCalculatorService).calculateFare(ticket);

        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // Act
            parkingService.processExitingVehicle();

            // Assert
            verify(ticketDAO).getTicket(vehicleRegNumber);
            assertNotNull(ticket.getOutTime());
            verify(fareCalculatorService).calculateFare(ticket);
            verify(ticketDAO).updateTicket(ticket);
            verify(parkingSpotDAO).updateParking(parkingSpotCaptor.capture());
            assertTrue(parkingSpotCaptor.getValue().isAvailable());
            assertTrue(outContent.toString().contains("1.5€"));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void processExitingVehicleWithDiscountCaseTest() throws Exception {
        // Arrange
        Ticket ticket = createTestTicket();
        when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTickets(vehicleRegNumber)).thenReturn(2);

        doAnswer(invocation -> {
            final Ticket capturedTicket = invocation.getArgument(0);
            capturedTicket.setPrice(1.43);
            return null;
        }).when(fareCalculatorService).calculateFare(ticket, true);

        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // Act
            parkingService.processExitingVehicle();

            // Assert
            verify(fareCalculatorService).calculateFare(ticket, true);
            assertTrue(outContent.toString().contains("1.43€"));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void processIncomingVehicleNominalCaseTest() throws Exception {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.getTicket(vehicleRegNumber)).thenReturn(null);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // Act
            parkingService.processIncomingVehicle();

            // Assert
            verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
            verify(ticketDAO).getTicket(vehicleRegNumber);
            verify(parkingSpotDAO).updateParking(parkingSpotCaptor.capture());
            assertFalse(parkingSpotCaptor.getValue().isAvailable());

            verify(ticketDAO).saveTicket(ticketCaptor.capture());
            assertNotNull(ticketCaptor.getValue().getParkingSpot());
            assertEquals(ticketCaptor.getValue().getVehicleRegNumber(), vehicleRegNumber);
            assertEquals(ticketCaptor.getValue().getPrice(), 0);
            assertNotNull(ticketCaptor.getValue().getInTime());
            assertNull(ticketCaptor.getValue().getOutTime());

            assertTrue(outContent.toString().contains("Please park your vehicle in spot number: 1"));
        } finally {
            System.setOut(System.out);
        }
    }

    private Ticket createTestTicket() {
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber(vehicleRegNumber);
        return ticket;
    }

}
