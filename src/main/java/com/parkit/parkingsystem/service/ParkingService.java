package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private FareCalculatorService fareCalculatorService;
    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private  TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO
    , FareCalculatorService fareCalculatorService){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
        this.fareCalculatorService = fareCalculatorService;
    }

    public void processIncomingVehicle() {
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();

            if (parkingSpot !=null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehicleRegNumber();
                Ticket existingTicket = ticketDAO.getTicket(vehicleRegNumber);
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot); //allot this parking space and mark its availability as false
                Date inTime = new Date();
                Ticket newTicket = new Ticket();
                //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME
                newTicket.setParkingSpot(parkingSpot);
                newTicket.setVehicleRegNumber(vehicleRegNumber);
                newTicket.setPrice(0);
                newTicket.setInTime(inTime);
                newTicket.setOutTime(null);
                ticketDAO.saveTicket(newTicket);

                if (existingTicket != null) {
                    System.out.println("\nHappy to see you again! As a regular user of our parking lot, you will enjoy a 5% discount.\n");
                }

                System.out.println("Generated Ticket and saved in DB\n");
                System.out.println("Please park your vehicle in spot number: "+parkingSpot.getId());
                System.out.println("\nRecorded in-time for vehicle number: " + vehicleRegNumber+" is: " + inTime + '\n');
            }

        }catch(Exception e){
            logger.error("Unable to process incoming vehicle",e);
        }
    }

    private String getVehicleRegNumber() throws Exception {
        System.out.println("\nPlease type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    private ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            }else{
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }catch(Exception e){
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    private ParkingType getVehicleType(){
        System.out.println("\nPlease select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE\n");
        int input = inputReaderUtil.readSelection();
        return switch (input) {
            case 1 -> ParkingType.CAR;
            case 2 -> ParkingType.BIKE;
            default -> {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        };
    }

    public void processExitingVehicle() {
        try{
            String vehicleRegNumber = getVehicleRegNumber();
            Ticket existingTicket = ticketDAO.getTicket(vehicleRegNumber);
            Date outTime = new Date();
            existingTicket.setOutTime(outTime);
            boolean isDiscounted = ticketDAO.getNbTickets(vehicleRegNumber) > 1;

            if (isDiscounted) {
                fareCalculatorService.calculateFare(existingTicket, isDiscounted);
            } else {
                fareCalculatorService.calculateFare(existingTicket);
            }

            if(ticketDAO.updateExitTicket(existingTicket)) {
                ParkingSpot parkingSpot = existingTicket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                displayExitMessage(existingTicket, isDiscounted);
            }else{
                System.out.println("Unable to update ticket information. Error occurred");
            }

        }catch(Exception e){
            logger.error("Unable to process exiting vehicle",e);
        }
    }

    private void displayExitMessage(Ticket ticket, boolean isDiscounted) {
        if (ticket.getPrice() == 0.0) {
            System.out.println("\nThank you for using our parking. No payment needed.\n");
        } else if (isDiscounted) {
            System.out.println("\nPlease pay the parking fare (5% discount included): " + ticket.getPrice() + "€\n");
        } else {
            System.out.println("\nPlease pay the parking fare: " + ticket.getPrice() + "€\n");
        }
        System.out.println("Recorded out-time for vehicle number " + ticket.getVehicleRegNumber() + " is: "
                + ticket.getOutTime() + "\n");
    }

}
