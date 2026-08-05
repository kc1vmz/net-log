package com.kc1vmz.netlog.accessor;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.object.Location;
import com.kc1vmz.netlog.object.LocationCountry;
import com.kc1vmz.netlog.object.LocationCounty;
import com.kc1vmz.netlog.object.LocationState;
import com.kc1vmz.netlog.record.LocationCountryRecord;
import com.kc1vmz.netlog.record.LocationCountyRecord;
import com.kc1vmz.netlog.record.LocationMunicipalityRecord;
import com.kc1vmz.netlog.record.LocationStateRecord;
import com.kc1vmz.netlog.repository.LocationCountryRepository;
import com.kc1vmz.netlog.repository.LocationCountyRepository;
import com.kc1vmz.netlog.repository.LocationMunicipalityRepository;
import com.kc1vmz.netlog.repository.LocationStateRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LocationAccessor {
    @Inject
    private LocationCountryRepository locationCountryRepository;
    @Inject
    private LocationStateRepository locationStateRepository;
    @Inject
    private LocationCountyRepository locationCountyRepository;
    @Inject
    private LocationMunicipalityRepository locationMunicipalityRepository;
    private static final Logger logger = LogManager.getLogger(LocationAccessor.class);


    /* country */
    public List<LocationCountry> listCountries() {
        List<LocationCountry> ret = new ArrayList<>();
        try {
            List<LocationCountryRecord> records =  locationCountryRepository.findAll();
            if (records != null) {
                for (LocationCountryRecord record : records) {
                    LocationCountry res = new LocationCountry();
                    res.setId(record.id());
                    res.setName(record.name());
                    res.setAbbreviation(record.abbreviation());
                    ret.add(res);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<LocationCountry>() {
            @Override
            public int compare(LocationCountry obj1, LocationCountry obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });
        return ret;
    }

    public LocationCountry createCountry(LocationCountry res) {
        try {
            LocationCountryRecord rec = new LocationCountryRecord(UUID.randomUUID().toString(), res.getName(), res.getAbbreviation());
            LocationCountryRecord recNew = locationCountryRepository.save(rec);
            res.setId(recNew.id());
            return getCountry(recNew.id());
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public LocationCountry getCountry(String id) {
        LocationCountry ret = null;

        try {
            Optional<LocationCountryRecord> recordOpt =  locationCountryRepository.findById(id);
            if (recordOpt.isPresent()) {
                LocationCountryRecord record = recordOpt.get();
                ret = new LocationCountry();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setAbbreviation(record.abbreviation());
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public void deleteCountry(String id) {
        locationCountryRepository.deleteById(id);
    }

    public LocationCountry updateCountry(LocationCountry obj) {
        try {
            Optional<LocationCountryRecord> recordOpt =  locationCountryRepository.findById(obj.getId());
            if (!recordOpt.isPresent()) {
                return null;
            }

            LocationCountryRecord rec = recordOpt.get();
            LocationCountryRecord recUpdate = new LocationCountryRecord(rec.id(), obj.getName(), obj.getAbbreviation());
            locationCountryRepository.update(recUpdate);
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }


    /* state */
    public List<LocationState> listStates(LocationCountry country) {
        List<LocationState> ret = new ArrayList<>();
        if ((country == null) || (country.getId() == null)) {
            return ret;
        }

        if (country.getName() == null) {
            country = getCountry(country.getId());
        }

        try {
            List<LocationStateRecord> records =  locationStateRepository.findBycountry_id(country.getId());
            if (records != null) {
                for (LocationStateRecord record : records) {
                    LocationState res = new LocationState();
                    res.setId(record.id());
                    res.setName(record.name());
                    res.setAbbreviation(record.abbreviation());
                    res.setCountry(country);
                    ret.add(res);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<LocationState>() {
            @Override
            public int compare(LocationState obj1, LocationState obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });
        return ret;
    }

    public LocationState createState(String countryId, LocationState res) {
        try {
            LocationStateRecord rec = new LocationStateRecord(UUID.randomUUID().toString(), countryId, res.getName(), res.getAbbreviation());
            LocationStateRecord recNew = locationStateRepository.save(rec);
            res.setId(recNew.id());
            return getState(recNew.id());
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public LocationState getState(String id) {
        LocationState ret = null;

        try {
            Optional<LocationStateRecord> recordOpt =  locationStateRepository.findById(id);
            if (recordOpt.isPresent()) {
                LocationStateRecord record = recordOpt.get();
                ret = new LocationState();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setAbbreviation(record.abbreviation());

                LocationCountry resCountry = getCountry(record.country_id());
                ret.setCountry(resCountry);
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public void deleteState(String id) {
        locationStateRepository.deleteById(id);
    }

    public LocationState updateState(LocationState obj) {
        try {
            Optional<LocationStateRecord> recordOpt =  locationStateRepository.findById(obj.getId());
            if (!recordOpt.isPresent()) {
                return null;
            }

            LocationStateRecord rec = recordOpt.get();
            LocationStateRecord recUpdate = new LocationStateRecord(rec.id(), obj.getCountry().getId(), obj.getName(), obj.getAbbreviation());
            locationStateRepository.update(recUpdate);
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    /* county */
    public List<LocationCounty> listCounties(LocationState state) {
        List<LocationCounty> ret = new ArrayList<>();
        if ((state == null) || (state.getId() == null)) {
            return ret;
        }

        if (state.getName() == null) {
            state = getState(state.getId());
        }

        try {
            List<LocationCountyRecord> records =  locationCountyRepository.findBystate_id(state.getId());
            if (records != null) {
                for (LocationCountyRecord record : records) {
                    LocationCounty res = new LocationCounty();
                    res.setId(record.id());
                    res.setName(record.name());
                    res.setState(state);
                    res.setCountry(state.getCountry());
                    ret.add(res);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<LocationCounty>() {
            @Override
            public int compare(LocationCounty obj1, LocationCounty obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });
        return ret;
    }

    public LocationCounty createCounty(LocationState state, LocationCounty res) {
        try {
            LocationCountyRecord rec = new LocationCountyRecord(UUID.randomUUID().toString(), state.getCountry().getId(), state.getId(), res.getName());
            LocationCountyRecord recNew = locationCountyRepository.save(rec);
            res.setId(recNew.id());
            return getCounty(recNew.id());
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public LocationCounty getCounty(String id) {
        LocationCounty ret = null;

        try {
            Optional<LocationCountyRecord> recordOpt =  locationCountyRepository.findById(id);
            if (recordOpt.isPresent()) {
                LocationCountyRecord record = recordOpt.get();
                ret = new LocationCounty();
                ret.setId(record.id());
                ret.setName(record.name());

                LocationCountry resCountry = getCountry(record.country_id());
                ret.setCountry(resCountry);
                LocationState resState = getState(record.state_id());
                ret.setState(resState);
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public void deleteCounty(String id) {
        locationCountyRepository.deleteById(id);
    }

    public LocationCounty updateCounty(LocationCounty obj) {
        try {
            Optional<LocationCountyRecord> recordOpt =  locationCountyRepository.findById(obj.getId());
            if (!recordOpt.isPresent()) {
                return null;
            }

            LocationCountyRecord rec = recordOpt.get();
            LocationCountyRecord recUpdate = new LocationCountyRecord(rec.id(), obj.getCountry().getId(), obj.getState().getId(), obj.getName());
            locationCountyRepository.update(recUpdate);
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    /* location / municipality */
    public List<Location> listLocations(LocationState state) {
        List<Location> ret = new ArrayList<>();
        Map<String, LocationCounty> countyCache = new HashMap<>();

        if ((state == null) || (state.getId() == null)) {
            return ret;
        }

        if (state.getName() == null) {
            state = getState(state.getId());
        }

        try {
            List<LocationMunicipalityRecord> records =  locationMunicipalityRepository.findBystate_id(state.getId());
            if (records != null) {
                for (LocationMunicipalityRecord record : records) {
                    Location res = new Location();
                    res.setId(record.id());
                    res.setName(record.name());
                    res.setState(state);
                    res.setCountry(state.getCountry());

                    if (record.county_id() != null) {
                        LocationCounty cachedCounty = countyCache.get(record.county_id());
                        if (cachedCounty != null) {
                            res.setCounty(cachedCounty);
                        } else {
                            LocationCounty resCounty = getCounty(record.county_id());
                            res.setCounty(resCounty);
                            countyCache.put(resCounty.getId(), resCounty);
                        }
                    }
                    ret.add(res);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<Location>() {
            @Override
            public int compare(Location obj1, Location obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });
        return ret;
    }

    public List<Location> listLocations(LocationCounty county) {
        List<Location> ret = new ArrayList<>();

        if ((county == null) || (county.getId() == null)) {
            return ret;
        }

        try {
            List<LocationMunicipalityRecord> records =  locationMunicipalityRepository.findBycounty_id(county.getId());
            if (records != null) {
                for (LocationMunicipalityRecord record : records) {
                    Location res = new Location();
                    res.setId(record.id());
                    res.setName(record.name());
                    res.setState(county.getState());
                    res.setCountry(county.getState().getCountry());
                    res.setCounty(county);
                    ret.add(res);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<Location>() {
            @Override
            public int compare(Location obj1, Location obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });
        return ret;
    }

    public Location createLocation(LocationState state, Location res) {
        try {
            LocationMunicipalityRecord rec = new LocationMunicipalityRecord(UUID.randomUUID().toString(), state.getCountry().getId(), state.getId(),
                                                                            (res.getCounty() != null) ? res.getCounty().getId() : null,
                                                                            res.getName());
            LocationMunicipalityRecord recNew = locationMunicipalityRepository.save(rec);
            res.setId(recNew.id());
            return getLocation(recNew.id());
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public Location getLocation(String id) {
        Location ret = null;

        try {
            Optional<LocationMunicipalityRecord> recordOpt =  locationMunicipalityRepository.findById(id);
            if (recordOpt.isPresent()) {
                LocationMunicipalityRecord record = recordOpt.get();
                ret = new Location();
                ret.setId(record.id());
                ret.setName(record.name());

                LocationCountry resCountry = getCountry(record.country_id());
                ret.setCountry(resCountry);
                LocationState resState = getState(record.state_id());
                ret.setState(resState);
                LocationCounty resCounty = getCounty(record.county_id());
                ret.setCounty(resCounty);
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public void deleteLocation(String id) {
        locationMunicipalityRepository.deleteById(id);
    }

    public Location updateLocation(Location obj) {
        try {
            Optional<LocationMunicipalityRecord> recordOpt =  locationMunicipalityRepository.findById(obj.getId());
            if (!recordOpt.isPresent()) {
                return null;
            }

            LocationMunicipalityRecord rec = recordOpt.get();
            LocationMunicipalityRecord recUpdate = new LocationMunicipalityRecord(rec.id(),
                                (obj.getCountry() != null && obj.getCountry().getId() != null) ? obj.getCountry().getId() : null,
                                (obj.getState() != null && obj.getState().getId() != null) ? obj.getState().getId() : null,
                                (obj.getCounty() != null && obj.getCounty().getId() != null) ? obj.getCounty().getId() : null,
                                obj.getName());
            locationMunicipalityRepository.update(recUpdate);
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public List<Location> listLocations(List<Location> locationsDistrict) {
        List<Location> ret = new ArrayList<>();

        for (Location location : locationsDistrict) {
            ret.add(getLocation(location.getId()));
        }
        return ret;
    }

    public LocationCountry getCountryByName(String countryName) {
        LocationCountry ret = null;

        try {
            Optional<LocationCountryRecord> recordOpt =  locationCountryRepository.findByname(countryName);
            if (recordOpt.isPresent()) {
                LocationCountryRecord record = recordOpt.get();
                ret = new LocationCountry();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setAbbreviation(record.abbreviation());
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public LocationState getStateByName(LocationCountry country, String stateName) {
        List<LocationState> states = listStates(country);
        for (LocationState state : states) {
            if (state.getName().equalsIgnoreCase(stateName)) {
                return state;
            }
        }
        return null;
    }

    public Location getLocationByName(LocationCountry country, LocationState state, String name) {
        List<Location> locations = listLocations(state);
        for (Location location : locations) {
            if (location.getName().equalsIgnoreCase(name)) {
                return location;
            }
        }
        return null;
    }
}
