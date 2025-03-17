package com.parkit.parkingsystem.util;

import java.util.Scanner;

public class InputReaderUtil {

    private Scanner scan;

    public InputReaderUtil(Scanner scanner) {
        this.scan = scanner;
    }

    public InputReaderUtil() {
        this.scan = new Scanner(System.in);
    }

    public int readSelection() {
        return Integer.parseInt(scan.nextLine());
    }

    public String readVehicleRegistrationNumber() {
            String vehicleRegNumber= scan.nextLine();
            if (vehicleRegNumber == null || vehicleRegNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("\nInvalid vehicle type input: " + vehicleRegNumber);
            }
            return vehicleRegNumber;
    }

}
