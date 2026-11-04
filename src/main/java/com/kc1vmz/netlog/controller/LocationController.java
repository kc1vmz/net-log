package com.kc1vmz.netlog.controller;

/*
    NetLog
    Copyright (c) 2026 John Rokicki KC1VMZ

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
    
    http://www.kc1vmz.com
*/

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import com.kc1vmz.netlog.accessor.LocationAccessor;
import com.kc1vmz.netlog.object.Location;
import com.kc1vmz.netlog.object.LocationCountry;
import com.kc1vmz.netlog.object.LocationCounty;
import com.kc1vmz.netlog.object.LocationState;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    @Autowired
    private LocationAccessor locationAccessor;

    @GetMapping("/countries")
    public List<LocationCountry> listCountries() {
        return locationAccessor.listCountries();
    }

    @GetMapping("/countries/{id}")
    public LocationCountry getCountry(@PathVariable String id) {
        return locationAccessor.getCountry(id);
    }

    @PutMapping("/countries/{id}")
    public LocationCountry update(@PathVariable String id, @RequestBody LocationCountry obj) {
        return locationAccessor.updateCountry(obj);
    }

    @DeleteMapping("/countries/{id}")
    public void delete(@PathVariable String id) {
        locationAccessor.deleteCountry(id);
    }

    @PostMapping("/countries")
    public ResponseEntity<LocationCountry> create(@RequestBody LocationCountry obj) {
        LocationCountry objNew = locationAccessor.createCountry(obj);
        if (objNew == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(201).body(objNew);
    }

    @GetMapping("/countries/{countryId}/states")
    public List<LocationState> getStates(@PathVariable String countryId){
        LocationCountry country = locationAccessor.getCountry(countryId);
        return locationAccessor.listStates(country);
    }

    @GetMapping("/countries/{countryId}/states/{id}")
    public LocationState getState(@PathVariable String countryId, @PathVariable String id) {
        return locationAccessor.getState(id);
    }

    @PutMapping("/countries/{countryId}/states/{id}")
    public LocationState updateState(@PathVariable String countryId, @PathVariable String id, @RequestBody LocationState obj) {
        return locationAccessor.updateState(obj);
    }

    @DeleteMapping("/countries/{countryId}/states/{id}")
    public void deleteState(@PathVariable String countryId, @PathVariable String id) {
        locationAccessor.deleteState(id);
    }

    @PostMapping("/countries/{countryId}/states")
    public ResponseEntity<LocationState> createState(@PathVariable String countryId, @RequestBody LocationState obj) {
        LocationState objNew = locationAccessor.createState(countryId, obj);
        if (objNew == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(201).body(objNew);
    }

    @GetMapping("/countries/{countryId}/states/{stateId}/municipalities")
    public List<Location> getMunicipalities(@PathVariable String countryId, @PathVariable String stateId){
        LocationState state = locationAccessor.getState(stateId);
        return locationAccessor.listLocations(state);
    }

    @GetMapping("/countries/{countryId}/states/{stateId}/municipalities/{id}")
    public Location getMunicipality(@PathVariable String countryId, @PathVariable String stateId, @PathVariable String id) {
        return locationAccessor.getLocation(id);
    }

    @PutMapping("/countries/{countryId}/states/{stateId}/municipalities/{id}")
    public Location updateMunicipality(@PathVariable String countryId, @PathVariable String stateId, @PathVariable String id, @RequestBody Location obj) {
        return locationAccessor.updateLocation(obj);
    }

    @DeleteMapping("/countries/{countryId}/states/{stateId}/municipalities/{id}")
    public void deleteMunicipality(@PathVariable String countryId, @PathVariable String stateId, @PathVariable String id) {
        locationAccessor.deleteLocation(id);
    }

    @PostMapping("/countries/{countryId}/states/{stateId}/municipalities")
    public ResponseEntity<Location> createMunicipality(@PathVariable String countryId, @PathVariable String stateId, @RequestBody Location obj) {
        LocationState state = locationAccessor.getState(stateId);
        Location objNew = locationAccessor.createLocation(state, obj);
        if (objNew == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(201).body(objNew);
    }

    @GetMapping("/countries/{countryId}/states/{stateId}/counties")
    public List<LocationCounty> getCounties(@PathVariable String countryId, @PathVariable String stateId){
        LocationState state = locationAccessor.getState(stateId);
        return locationAccessor.listCounties(state);
    }

    @GetMapping("/countries/{countryId}/states/{stateId}/counties/{id}")
    public LocationCounty getCounty(@PathVariable String countryId, @PathVariable String stateId, @PathVariable String id) {
        return locationAccessor.getCounty(id);
    }

    @PutMapping("/countries/{countryId}/states/{stateId}/counties/{id}")
    public LocationCounty updateCounty(@PathVariable String countryId, @PathVariable String stateId, @PathVariable String id, @RequestBody LocationCounty obj) {
        return locationAccessor.updateCounty(obj);
    }

    @DeleteMapping("/countries/{countryId}/states/{stateId}/counties/{id}")
    public void deleteCounty(@PathVariable String countryId, @PathVariable String stateId, @PathVariable String id) {
        locationAccessor.deleteCounty(id);
    }

    @PostMapping("/countries/{countryId}/states/{stateId}/counties")
    public ResponseEntity<LocationCounty> createCounty(@PathVariable String countryId, @PathVariable String stateId, @RequestBody LocationCounty obj) {
        LocationState state = locationAccessor.getState(stateId);
        LocationCounty objNew = locationAccessor.createCounty(state, obj);
        if (objNew == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(201).body(objNew);
    }

    @GetMapping("/countries/{countryId}/states/{stateId}/counties/{countyId}/municipalities")
    public List<Location> getCountyMunicipalities(@PathVariable String countryId, @PathVariable String stateId, @PathVariable String countyId) {
        LocationCounty county = locationAccessor.getCounty(countyId);
        return locationAccessor.listLocations(county);
    }

}