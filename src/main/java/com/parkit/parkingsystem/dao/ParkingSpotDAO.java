package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.exceptions.DatabaseException;
import com.parkit.parkingsystem.exceptions.NoAvailableSlotException;
import com.parkit.parkingsystem.model.ParkingSpot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParkingSpotDAO {

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public int getNextAvailableSlot(ParkingType parkingType){
        try (
                Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)
        ) {
            ps.setString(1, parkingType.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException ex) { 
            throw new DatabaseException("Error while accessing the database", ex); 
        }
    
        throw new NoAvailableSlotException("No available slot found for parking type: " + parkingType);
    }


    public boolean updateParking(ParkingSpot parkingSpot){
        try (
            Connection con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
        ){
            ps.setBoolean(1, parkingSpot.isAvailable());
            ps.setInt(2, parkingSpot.getId());

            int updateRowCount = ps.executeUpdate();
            return updateRowCount == 1;

        }catch (SQLException ex){
            throw new DatabaseException("Error while accessing the database", ex);
        }
    }

}
