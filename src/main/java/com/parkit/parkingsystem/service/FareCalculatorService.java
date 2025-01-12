package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean isDiscounted) {

        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ) {
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();
        long durationInMillis = outTimeMillis - inTimeMillis;
        double durationInHours = durationInMillis / (1000.0 * 60 * 60);

        if(durationInHours < 0.5) {
            ticket.setPrice(0.0);
        }else {
            double ratePerHour = switch (ticket.getParkingSpot().getParkingType()) {
                case CAR -> Fare.CAR_RATE_PER_HOUR;
                case BIKE -> Fare.BIKE_RATE_PER_HOUR;
                default -> throw new IllegalArgumentException("Unknown Parking Type");
            };

            double price = durationInHours * ratePerHour;
            if (isDiscounted) {
                price *= 0.95;
            }

            ticket.setPrice(BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).doubleValue());
        }
    }
}