package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.City
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface CityDao : CrudRepository<City, Int> {
    @Query("SELECT c FROM City c WHERE " +
            ":longitude > c.minLongitude AND :longitude < c.maxLongitude AND " +
            ":latitude > c.minLatitude AND :latitude < c.maxLatitude")
    fun findByLocation(@Param("longitude") longitude: Double, @Param("latitude") latitude: Double): City?
}