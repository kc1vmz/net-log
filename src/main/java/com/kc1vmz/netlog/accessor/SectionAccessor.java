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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.object.District;
import com.kc1vmz.netlog.object.Location;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.record.DistrictLocationRecord;
import com.kc1vmz.netlog.record.DistrictRecord;
import com.kc1vmz.netlog.record.SectionRecord;
import com.kc1vmz.netlog.repository.DistrictLocationRepository;
import com.kc1vmz.netlog.repository.DistrictRepository;
import com.kc1vmz.netlog.repository.SectionRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SectionAccessor {
    @Inject
    private SectionRepository sectionRepository;
    @Inject
    private DistrictRepository districtRepository;
    @Inject
    private DistrictLocationRepository districtLocationRepository;
    private static final Logger logger = LogManager.getLogger(SectionAccessor.class);

    public List<Section> list() {
        return list(true);
    }

    public List<Section> list(boolean activeOnly) {
        List<Section> ret = new ArrayList<>();

        try {
            List<SectionRecord> records =  sectionRepository.findAll();
            if (records != null) {
                for (SectionRecord record : records) {
                    if (activeOnly) {
                        if (!record.active()) {
                            continue;
                        }
                    }
                    Section section = new Section();
                    section.setId(record.id());
                    section.setName(record.name());
                    section.setDescription(record.description());
                    section.setActive(record.active());
                    ret.add(section);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<Section>() {
            @Override
            public int compare(Section obj1, Section obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });

        return ret;
    }

    public Section create(Section section) {
        try {
            SectionRecord rec = new SectionRecord(UUID.randomUUID().toString(), section.getName(), section.getDescription(), true);
            SectionRecord recNew = sectionRepository.save(rec);
            section.setId(recNew.id());
            return get(recNew.id());
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public Section get(String id) {
        Section ret = null;

        try {
            Optional<SectionRecord> recordOpt =  sectionRepository.findById(id);
            if (recordOpt.isPresent()) {
                SectionRecord record = recordOpt.get();
                ret = new Section();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setDescription(record.description());
                ret.setActive(record.active());
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public void delete(String id, boolean soft) {
        Section section = get(id);
        if (soft) {
            try {
                section.setActive(false);
                update(id, section);
            } catch (Exception e) {
            }
        } else {
            List<District> districts = listDistricts(section);
            for (District district : districts) {
                removeDistrict(district);
            }
            sectionRepository.deleteById(id);
        }
    }

    public Section update(String id, Section obj) {
        try {
            Optional<SectionRecord> recordOpt =  sectionRepository.findById(id);
            if (!recordOpt.isPresent()) {
                return null;
            }

            SectionRecord rec = recordOpt.get();
            SectionRecord recUpdate = new SectionRecord(rec.id(), obj.getName(), obj.getDescription(), obj.isActive());
            sectionRepository.update(recUpdate);
            obj.setId(id);
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public List<District> listDistricts(Section section) {
        List<District> ret = new ArrayList<>();

        try {
            List<DistrictRecord> records =  districtRepository.findBysection_id(section.getId());
            if (records != null) {
                for (DistrictRecord record : records) {
                    District district = getDistrict(record.id());
                    ret.add(district);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<District>() {
            @Override
            public int compare(District obj1, District obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });

        return ret;
    }

    public District getDistrict(String id) {
        District ret = null;

        try {
            Optional<DistrictRecord> recordOpt =  districtRepository.findById(id);
            if (recordOpt.isPresent()) {
                DistrictRecord record = recordOpt.get();
                ret = new District();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setDescription(record.description());
                ret.setSectionId(record.section_id());
                ret.setLeaderName(record.leader_name());
                ret.setLeaderContact(record.leader_contact());
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public District addDistrict(Section section, District district) {
        // duplicate check
        List<District> districts = listDistricts(section);
        for (District districtIter : districts) {
            if (districtIter.getName().equalsIgnoreCase(district.getName())) {
                return districtIter;
            }
        }

        try {
            DistrictRecord rec = new DistrictRecord(UUID.randomUUID().toString(), section.getId(), district.getName(), district.getDescription(), district.getLeaderName(), district.getLeaderContact());
            DistrictRecord recNew = districtRepository.save(rec);
            district.setId(recNew.id());
            return getDistrict(district.getId());
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public void removeDistrict(District district) {
        // delete all district to location records
        try {
            List<DistrictLocationRecord> records =  districtLocationRepository.findBydistrict_id(district.getId());
            if (records != null) {
                for (DistrictLocationRecord record : records) {
                    districtLocationRepository.delete(record);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        // delete the district - it cannot be used anywhere else
        try {
            Optional<DistrictRecord> recordOpt =  districtRepository.findById(district.getId());
            if (recordOpt.isPresent()) {
                districtRepository.delete(recordOpt.get());
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
    }

    public District updateDistrict(District obj) {
        try {
            Optional<DistrictRecord> recordOpt =  districtRepository.findById(obj.getId());
            if (!recordOpt.isPresent()) {
                return null;
            }

            DistrictRecord rec = recordOpt.get();
            DistrictRecord recUpdate = new DistrictRecord(rec.id(), rec.section_id(), obj.getName(), obj.getDescription(), obj.getLeaderName(), obj.getLeaderContact());
            districtRepository.update(recUpdate);
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public boolean addLocationToDistrict(District district, Location location) {
        // duplicate check
        List<Location> locations = listLocations(district);
        for (Location locationIter : locations) {
            if (locationIter.getId().equalsIgnoreCase(location.getId())) {
                return true;
            }
        }

        try {
            DistrictLocationRecord rec = new DistrictLocationRecord(UUID.randomUUID().toString(), district.getSectionId(), district.getId(), location.getId());
            districtLocationRepository.save(rec);
            return true;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return false;
    }

    public List<Location> listLocations(District district) {
        List<Location> ret = new ArrayList<>();

        try {
            List<DistrictLocationRecord> records =  districtLocationRepository.findBydistrict_id(district.getId());
            if (records != null) {
                for (DistrictLocationRecord record : records) {
                    Location location = new Location();
                    location.setId(record.location_id());
                    ret.add(location);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public void removeMunicipalityFromDistrict(District district, Location location) {
        try {
            List<DistrictLocationRecord> records =  districtLocationRepository.findBydistrict_id(district.getId());
            if (records != null) {
                for (DistrictLocationRecord record : records) {
                    if (record.location_id().equals(location.getId())) {
                        // remove this one
                        districtLocationRepository.delete(record);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
    }

}
