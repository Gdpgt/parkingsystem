package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.constants.ParkingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketDAOTest {

    private TicketDAO ticketDAO;

    @Mock
    private DataBaseConfig dataBaseConfig;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private Ticket testTicket;

    @BeforeEach
    void setUp() throws Exception {
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseConfig;

        testTicket = new Ticket();
        testTicket.setId(1);
        testTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        testTicket.setVehicleRegNumber("ABCDEF");
        testTicket.setPrice(1.5);
        testTicket.setInTime(new Date());
        testTicket.setOutTime(new Date());

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void saveTicketWhenInsertSucceeds() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = ticketDAO.saveTicket(testTicket);

        // Assert
        assertTrue(result);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void saveTicketWhenSQLExceptionOccurs() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = ticketDAO.saveTicket(testTicket);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void getTicketWhenVehicleExists() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getInt(2)).thenReturn(1);
        when(resultSet.getDouble(3)).thenReturn(1.5);
        when(resultSet.getTimestamp(4)).thenReturn(new java.sql.Timestamp(testTicket.getInTime().getTime()));
        when(resultSet.getTimestamp(5)).thenReturn(new java.sql.Timestamp(testTicket.getOutTime().getTime()));
        when(resultSet.getString(6)).thenReturn("CAR");

        // Act
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        // Assert
        assertNotNull(ticket);
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertEquals(1.5, ticket.getPrice());
        assertEquals(1, ticket.getParkingSpot().getId());
        assertEquals(ParkingType.CAR, ticket.getParkingSpot().getParkingType());
        assertEquals(testTicket.getInTime().getTime(), ticket.getInTime().getTime(), 1000);
        assertEquals(testTicket.getOutTime().getTime(), ticket.getOutTime().getTime(), 1000);


        verify(preparedStatement).setString(1, "ABCDEF");
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void getTicketWhenVehicleNotFound() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        Ticket ticket = ticketDAO.getTicket("UNKNOWN_CAR");

        // Assert
        assertNull(ticket);
        verify(preparedStatement).setString(1, "UNKNOWN_CAR");
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void updateExitTicketWhenUpdateSucceeds() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = ticketDAO.updateExitTicket(testTicket);

        // Assert
        assertTrue(result);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void updateExitTicketWhenSQLExceptionOccurs() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = ticketDAO.updateExitTicket(testTicket);

        // Assert
        assertFalse(result);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void getNbTicketsWhenTicketsFound() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);

        // Act
        int count = ticketDAO.getNbTickets("ABCDEF");

        // Assert
        assertEquals(3, count);
        verify(preparedStatement).setString(1, "ABCDEF");
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void getNbTicketsWhenNoTicketsFound() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        int count = ticketDAO.getNbTickets("ABCDEF");

        // Assert
        assertEquals(0, count);
        verify(preparedStatement).setString(1, "ABCDEF");
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

}
