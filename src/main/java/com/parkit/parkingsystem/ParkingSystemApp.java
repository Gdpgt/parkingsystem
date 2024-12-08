package com.parkit.parkingsystem;

import com.parkit.parkingsystem.service.InteractiveShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParkingSystemApp {
    private static final Logger logger = LogManager.getLogger("ParkingSystemApp");

    public static void main(String[] args){
        logger.info("Initializing Parking System");
        try {
            InteractiveShell.loadInterface();
        } catch (Exception e) {
            logger.error("Error occurred while loading the parking interface", e);
        }

    }
}
