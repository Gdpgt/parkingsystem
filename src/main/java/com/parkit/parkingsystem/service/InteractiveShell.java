package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.exceptions.DatabaseException;
import com.parkit.parkingsystem.exceptions.NoAvailableSlotException;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractiveShell {

    private static final Logger logger = LogManager.getLogger("InteractiveShell");

    public static void loadInterface(){
        logger.info("ParkingSystemApp initialized");
        System.out.println("\nWelcome to Parking System!\n");

        boolean continueApp = true;
        InputReaderUtil inputReaderUtil = new InputReaderUtil();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        TicketDAO ticketDAO = new TicketDAO();
        FareCalculatorService fareCalculatorService = new FareCalculatorService();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, fareCalculatorService);

        while(continueApp){
            loadMenu();
            int option;

            try {
                option = inputReaderUtil.readSelection();
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input: please enter a valid number.\n");
                continue;
            }

            switch(option){
                case 1 -> {
                    try {
                        parkingService.processIncomingVehicle();
                    } catch (IllegalArgumentException e) {
                        System.out.println("\nInvalid entry: please try again.");
                    } catch (DatabaseException e) { 
                        System.out.println("\nAn unexpected error has occurred. We are actively working to resolve the issue.");
                    } catch (NoAvailableSlotException e) { 
                        System.out.println("\nSorry, the parking is full for your type of vehicle. Please come back later.");
                    } catch (RuntimeException e) {
                        logger.error("\nUnexpected error occurred", e);
                        System.out.println("\nAn unexpected error has occurred. We are actively working to resolve the issue.");
                    }
                }
                case 2 -> {
                    try {
                        parkingService.processExitingVehicle();
                    } catch (NullPointerException e) {
                        System.out.println("\nInvalid entry: please try again.");
                    }
                }
                case 3 -> {
                    System.out.println("\nExiting the system.");
                    continueApp = false;
                }
                default -> System.out.println("\nUnsupported option. Please enter a number corresponding to the menu.");
            } 
        }
    }

    private static void loadMenu(){
        System.out.println("\nPlease select an option. Simply enter the number to choose an action");
        System.out.println("1 New Vehicle Entering - Allocate Parking Space");
        System.out.println("2 Vehicle Exiting - Generate Ticket Price");
        System.out.println("3 Shutdown System\n");
    }

}
