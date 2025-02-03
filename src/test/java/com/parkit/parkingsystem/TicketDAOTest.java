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
        testTicket.setVehicleRegNumber("ABC123");
        testTicket.setPrice(10.0);
        testTicket.setInTime(new Date());
        testTicket.setOutTime(new Date());

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void getTicket_shouldReturnNull_whenVehicleNotFound() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        Ticket ticket = ticketDAO.getTicket("UNKNOWN_CAR");

        // Assert
        assertNull(ticket);
        verify(preparedStatement).setString(1, "UNKNOWN_CAR");
    }

    @Test
    void saveTicket_shouldReturnFalse_whenSQLExceptionOccurs() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = ticketDAO.saveTicket(testTicket);

        // Assert
        assertFalse(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void updateExitTicket_shouldReturnFalse_whenSQLExceptionOccurs() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act
        boolean result = ticketDAO.updateExitTicket(testTicket);

        // Assert
        assertFalse(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }
}
