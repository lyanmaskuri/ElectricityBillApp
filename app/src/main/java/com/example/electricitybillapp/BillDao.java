package com.example.electricitybillapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BillDao {

    @Insert
    void insert(Bill bill);

    @Update
    void update(Bill bill);

    @Delete
    void delete(Bill bill);

    @Query("SELECT * FROM bill ORDER BY id ASC")
    List<Bill> getAll();

    @Query("SELECT * FROM bill WHERE id = :id")
    Bill getBillById(int id);

}