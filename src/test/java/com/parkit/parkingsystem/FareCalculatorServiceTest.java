package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    boolean isDiscounted = true;

    @BeforeAll
    public static void setUp() { fareCalculatorService = new FareCalculatorService(); }

    @BeforeEach
    public void setUpPerTest() { ticket = new Ticket(); }

    @Test
    void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60L * 60 * 1000) ); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice(), 0.01);
    }

    @Test
    void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60L * 60 * 1000) ); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(4, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice(), 0.01);
    }

    @Test
    void calculateFareUnknownType(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60L * 60 * 1000)); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60L * 60 * 1000)); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(4, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45L * 60 * 1000)); // 45 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(4, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice(), 0.01 );
    }

    @Test
    void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45L * 60 * 1000)); //45 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice(), 0.01 );
    }

    @Test
    void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (25L * 60 * 60 * 1000)); // 25 heures
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (25.0 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice(), 0.01 );
    }

    @Test
    void calculateFareCarWithLessThan30minutesParkingTime(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (25L * 60 * 1000)); // 25 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( 0.0, ticket.getPrice(), 0.01 );
    }

    @Test
    void calculateFareBikeWithLessThan30minutesParkingTime(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (25L * 60 * 1000)); // 25 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(4, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( 0.0, ticket.getPrice(), 0.01 );
    }

    @Test
        void calculateFareCarWithDiscount(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45L * 60 * 1000)); // 45 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, isDiscounted);
        assertEquals(0.75 * Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice(), 0.01);
    }

    @Test
    void calculateFareBikeWithDiscount(){
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45L * 60 * 1000)); // 45 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, isDiscounted);
        assertEquals(0.75 * Fare.BIKE_RATE_PER_HOUR * 0.95, ticket.getPrice(), 0.01);
    }

    @Test
    void calculateFareCarWithExactly30MinParkingTime() {
        // Arrange
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (30L * 60 * 1000)); // 30 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Act
        fareCalculatorService.calculateFare(ticket);

        // Assert
        assertEquals(0.0, ticket.getPrice(), 0.01);
    }

    @Test
    void calculateFareCarWithOneYearParkingTime() {
        // Arrange
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (8760L * 60 * 60 * 1000)); // 1 année (8760h)
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        // Act
        fareCalculatorService.calculateFare(ticket);

        // Assert
        assertEquals(8760.0 * Fare.CAR_RATE_PER_HOUR, ticket.getPrice(), 0.01);
    }

}
