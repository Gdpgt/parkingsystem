package com.parkit.parkingsystem;

import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class InputReaderUtilTest {

    private InputReaderUtil inputReaderUtil;

    @BeforeEach
    void setUp() {
        // Scanner mocké avec une entrée simulée
        Scanner testScanner = new Scanner(new ByteArrayInputStream("invalid\n".getBytes()));
        inputReaderUtil = new InputReaderUtil(testScanner);
    }

    @Test
    void readSelection_shouldReturnNegativeOne_whenInputIsInvalid() {
        // Act
        int result = inputReaderUtil.readSelection();

        // Assert
        assertEquals(-1, result, "Si l'entrée est invalide, readSelection() doit retourner -1.");
    }

    @Test
    void readVehicleRegistrationNumber_shouldThrowException_whenInputIsEmpty() {
        // Arrange
        Scanner emptyScanner = new Scanner(new ByteArrayInputStream("\n".getBytes()));
        inputReaderUtil = new InputReaderUtil(emptyScanner);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> inputReaderUtil.readVehicleRegistrationNumber());
        assertTrue(exception.getMessage().contains("Invalid input provided"));
    }
}
