package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

import java.util.Date;

public class ParkingService {

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
        ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
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
            System.out.println("\nHappy to see you again! As a regular user of our parking lot," 
            + " you will enjoy a 5% discount.\n");
        }

        System.out.println("\nYour ticket has been generated.\n");
        System.out.println("Please park your vehicle in spot number: "+parkingSpot.getId());
        System.out.println("\nRecorded entry time for vehicle number " + vehicleRegNumber+" : " + inTime + '\n');
    }


    private String getVehicleRegNumber() {
        System.out.println("\nPlease type the vehicle registration number and press the enter key :\n");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }


    private ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        ParkingType parkingType = getVehicleType();
        parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
        parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
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
            default -> throw new IllegalArgumentException("\nInvalid vehicle type input: " + input);
        };
    }


    public void processExitingVehicle() {
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
    }


    private void displayExitMessage(Ticket ticket, boolean isDiscounted) {
        if (ticket.getPrice() == 0.0) {
            System.out.println("\nThank you for using our parking. No payment needed (Parking time under 30 minutes).\n");
        } else if (isDiscounted) {
            System.out.println("\nPlease pay the parking fare (5% discount included): " + ticket.getPrice() + " euro(s)\n");
        } else {
            System.out.println("\nPlease pay the parking fare: " + ticket.getPrice() + " euro(s)\n");
        }
        System.out.println("Recorded out-time for vehicle number " + ticket.getVehicleRegNumber() + " is: "
                + ticket.getOutTime() + "\n");
    }

}
