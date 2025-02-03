package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
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
    void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void getNextAvailableSlot_shouldReturnParkingNumber_whenSlotAvailable() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);

        // Act
        int slot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Assert
        assertEquals(3, slot, "L'ID de la place de parking disponible doit être 3.");
        verify(preparedStatement).setString(1, "CAR");
    }

    @Test
    void getNextAvailableSlot_shouldReturnNegativeOne_whenNoSlotAvailable() throws Exception {
        // Arrange
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Aucun résultat trouvé

        // Act
        int slot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Assert
        assertEquals(-1, slot, "Si aucune place n'est dispo, la méthode doit retourner -1.");
    }

    @Test
    void updateParking_shouldReturnTrue_whenUpdateSuccessful() throws Exception {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Assert
        assertTrue(result, "L'update doit être réussi et retourner true.");
    }

    @Test
    void updateParking_shouldReturnFalse_whenUpdateFails() throws Exception {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        when(preparedStatement.executeUpdate()).thenReturn(0); // Aucun enregistrement mis à jour

        // Act
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // Assert
        assertFalse(result, "Si l'update échoue, la méthode doit retourner false.");
    }

    @Test
    void updateParking_shouldThrowException_whenSQLExceptionOccurs() throws Exception {
        // Arrange
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        assertFalse(parkingSpotDAO.updateParking(parkingSpot), "En cas d'erreur SQL, la méthode doit retourner false.");
    }
}
