package com.begoml.shares.logic.repositories.geo

import com.begoml.shares.logic.repositories.geo.models.UserRegion

interface GeoLocationRepository {

    suspend fun getRegionByIp(ip: String): UserRegion
}