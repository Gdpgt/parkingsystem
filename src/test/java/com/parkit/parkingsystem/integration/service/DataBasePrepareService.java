package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.exceptions.DatabaseException;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBasePrepareService {

    private static final Logger logger = LogManager.getLogger("DataBasePrepareService");
    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){
        try (Connection connection = dataBaseTestConfig.getConnection();
             PreparedStatement resetParking = connection.prepareStatement("UPDATE parking SET available = TRUE");
             PreparedStatement truncateTickets = connection.prepareStatement("TRUNCATE TABLE ticket")) {

            resetParking.executeUpdate();
            truncateTickets.executeUpdate();

            logger.info("Database entries cleared successfully.");

        } catch (SQLException ex) { 
            throw new DatabaseException("Error while clearing the database", ex); 
        }
    }


}
