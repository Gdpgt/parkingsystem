package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();

        long durationInMillis = outTimeMillis - inTimeMillis;

        double durationInHours = durationInMillis / (1000.0 * 60 * 60);

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                double price = durationInHours * Fare.CAR_RATE_PER_HOUR;
                ticket.setPrice(BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).doubleValue());
                break;
            }
            case BIKE: {
                double price = durationInHours * Fare.BIKE_RATE_PER_HOUR;
                ticket.setPrice(BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).doubleValue());
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}