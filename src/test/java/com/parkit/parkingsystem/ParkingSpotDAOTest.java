package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.exceptions.DatabaseException;
import com.parkit.parkingsystem.exceptions.NoAvailableSlotException;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSpotDAOTest {

    private ParkingSpotDAO parkingSpotDAO;

    @Mock
    private DataBaseConfig dataBaseConfig;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void getNextAvailableSlotWhenAvailable() throws SQLException {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);

        // Act
        int slot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Assert
        assertEquals(3, slot);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).setString(1, "CAR");
        verify(resultSet).next();
        verify(resultSet).getInt(1);
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void getNextAvailableSlotWhenNotAvailable() throws SQLException {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act & assert
        assertThrows(NoAvailableSlotException.class, () -> parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).setString(1, "CAR");
        verify(resultSet).next();
        verify(resultSet, never()).getInt(1);
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void getNextAvailableSlotWhenProblemWithDatabase() throws SQLException {
        // Arrange
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Act & assert
        assertThrows(DatabaseException.class, () -> parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
        verify(preparedStatement).setString(1, "CAR");
        verify(preparedStatement).executeQuery();
        verify(resultSet, never()).next();
        verify(resultSet, never()).getInt(1);
    }


    @Test
    void updateParkingWhenSuccessful() throws SQLException {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Assert
        assertTrue(result);
        verify(preparedStatement).setBoolean(1, parkingSpot.isAvailable());
        verify(preparedStatement).setInt(2, parkingSpot.getId());
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void updateParkingWhenProblemWithDatabase() throws SQLException {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act & assert
        assertThrows(DatabaseException.class, () -> parkingSpotDAO.updateParking(parkingSpot));
        verify(preparedStatement).setBoolean(1, parkingSpot.isAvailable());
        verify(preparedStatement).setInt(2, parkingSpot.getId());
        verify(preparedStatement).executeUpdate();
    }

}
