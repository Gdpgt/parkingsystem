package com.parkit.parkingsystem;

import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class InputReaderUtilTest {

    private InputReaderUtil inputReaderUtil;

    @Test
    void readSelectionValidInput() {
        // Arrange
        Scanner validScanner = new Scanner(new ByteArrayInputStream("2\n".getBytes()));
        inputReaderUtil = new InputReaderUtil(validScanner);

        // Act
        int result = inputReaderUtil.readSelection();

        // Assert
        assertEquals(2, result);
    }

    @Test
    void readSelectionInvalidInput() {
        // Arrange
        Scanner validScanner = new Scanner(new ByteArrayInputStream("invalid\n".getBytes()));
        inputReaderUtil = new InputReaderUtil(validScanner);

        // Act
        int result = inputReaderUtil.readSelection();

        // Assert
        assertEquals(-1, result);
    }

    @Test
    void readVehicleRegistrationNumberValidInput() throws Exception {
        // Arrange
        Scanner validScanner = new Scanner(new ByteArrayInputStream("ABCDEF\n".getBytes()));
        inputReaderUtil = new InputReaderUtil(validScanner);

        // Act
        String result = inputReaderUtil.readVehicleRegistrationNumber();

        // Assert
        assertEquals("ABCDEF", result);
    }

    @Test
    void readVehicleRegistrationNumberEmptyInput() {
        // Arrange
        Scanner emptyScanner = new Scanner(new ByteArrayInputStream("\n".getBytes()));
        inputReaderUtil = new InputReaderUtil(emptyScanner);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> inputReaderUtil.readVehicleRegistrationNumber());
        assertTrue(exception.getMessage().contains("Invalid input provided"));
    }
}
