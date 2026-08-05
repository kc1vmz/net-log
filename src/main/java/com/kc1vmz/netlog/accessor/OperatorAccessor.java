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

import com.kc1vmz.netlog.enums.MembershipType;
import com.kc1vmz.netlog.object.District;
import com.kc1vmz.netlog.object.Location;
import com.kc1vmz.netlog.object.Operator;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.object.SectionOperator;
import com.kc1vmz.netlog.record.OperatorRecord;
import com.kc1vmz.netlog.record.OperatorSectionRecord;
import com.kc1vmz.netlog.repository.OperatorRepository;
import com.kc1vmz.netlog.repository.OperatorSectionRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OperatorAccessor {
    @Inject
    private OperatorRepository operatorRepository;
    @Inject
    private OperatorSectionRepository operatorsSectionRepository;
    @Inject
    private LocationAccessor locationAccessor;
    private static final Logger logger = LogManager.getLogger(OperatorAccessor.class);

    public List<Operator> list() {
        List<Operator> ret = new ArrayList<>();

        try {
            List<OperatorRecord> records =  operatorRepository.findAll();
            if (records != null) {
                for (OperatorRecord record : records) {
                    Operator operator = new Operator();
                    operator.setId(record.id());
                    operator.setName(record.name());
                    operator.setCallsign(record.callsign());
                    operator.setNTS(record.isNTS());
                    operator.setSkywarn(record.isSkywarn());
                    operator.setRACES(record.isRACES());
                    if (record.location_municipality_id() != null) {
                        Location location = locationAccessor.getLocation(record.location_municipality_id());
                        operator.setLocation(location);
                    }
                    ret.add(operator);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<Operator>() {
            @Override
            public int compare(Operator obj1, Operator obj2) {
                return obj1.getCallsign().compareTo(obj2.getCallsign());
            }
        });

        return ret;
    }

    public Operator create(Operator operator) {
        Operator existing = null;
        try {
            existing = getByCallsign(operator.getCallsign());
        } catch (Exception e) {
        }

        if (existing != null) {
            return existing;
        }

        try {
            OperatorRecord rec = new OperatorRecord(UUID.randomUUID().toString(), operator.getCallsign(), operator.getName(), operator.isNTS(), operator.isSkywarn(), operator.isRACES(), 
                                                        (operator.getLocation() != null) ? operator.getLocation().getId() : null);
            OperatorRecord recNew = operatorRepository.save(rec);
            operator.setId(recNew.id());
            return operator;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public Operator get(String id) {
        Operator ret = null;

        try {
            Optional<OperatorRecord> recordOpt =  operatorRepository.findById(id);
            if (recordOpt.isPresent()) {
                OperatorRecord record = recordOpt.get();
                ret = new Operator();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setCallsign(record.callsign());
                ret.setNTS(record.isNTS());
                ret.setSkywarn(record.isSkywarn());
                ret.setRACES(record.isRACES());
                if (record.location_municipality_id() != null) {
                    Location location = locationAccessor.getLocation(record.location_municipality_id());
                    ret.setLocation(location);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public Operator getByCallsign(String id) {
        Operator ret = null;

        try {
            OperatorRecord record =  operatorRepository.findBycallsign(id);
            if (record != null) {
                ret = new Operator();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setCallsign(record.callsign());
                ret.setNTS(record.isNTS());
                ret.setSkywarn(record.isSkywarn());
                ret.setRACES(record.isRACES());
                if (record.location_municipality_id() != null) {
                    Location location = locationAccessor.getLocation(record.location_municipality_id());
                    ret.setLocation(location);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public void delete(String id) {
        operatorRepository.deleteById(id);
    }

    public Operator update(String id, Operator obj) {
        try {
            Optional<OperatorRecord> recordOpt =  operatorRepository.findById(id);
            if (!recordOpt.isPresent()) {
                return null;
            }

            OperatorRecord rec = recordOpt.get();
            OperatorRecord recNew = new OperatorRecord(rec.id(), obj.getCallsign(), obj.getName(), obj.isNTS(), obj.isSkywarn(), obj.isRACES(),
                                            (obj.getLocation() != null) ? obj.getLocation().getId() : null);
            OperatorRecord recSaved = operatorRepository.update(recNew);
            obj.setId(recSaved.id());
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public List<SectionOperator> listOperators(Section section) {
        List<SectionOperator> ret = new ArrayList<>();

        if ((section == null) || (section.getId() == null)) {
            return ret;
        }

        try {
            List<OperatorSectionRecord> records = operatorsSectionRepository.findBysectionId(section.getId());
            if (records != null) {
                for (OperatorSectionRecord record : records) {
                    Operator operator = get(record.operatorId());
                    SectionOperator sectionOperator = new SectionOperator(operator, record.membershipType(), section);
                    ret.add(sectionOperator);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<SectionOperator>() {
            @Override
            public int compare(SectionOperator obj1, SectionOperator obj2) {
                return obj1.getCallsign().compareTo(obj2.getCallsign());
            }
        });

        return ret;
    }

    public List<SectionOperator> listOperators(Section section, District district, List<Location> locations) {
        List<SectionOperator> ret = new ArrayList<>();

        if ((district == null) || (district.getId() == null)) {
            return ret;
        }

        try {
            List<OperatorSectionRecord> records = operatorsSectionRepository.findBysectionId(district.getSectionId());
            if (records != null) {
                for (OperatorSectionRecord record : records) {
                    Operator operator = get(record.operatorId());
                    for (Location location : locations) {
                        if ((operator.getLocation() != null) &&  (location.getId().equals(operator.getLocation().getId()))) {
                            // operator is in district
                            SectionOperator sectionOperator = new SectionOperator(operator, record.membershipType(), section);
                            ret.add(sectionOperator);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<SectionOperator>() {
            @Override
            public int compare(SectionOperator obj1, SectionOperator obj2) {
                return obj1.getCallsign().compareTo(obj2.getCallsign());
            }
        });

        return ret;
    }

    public List<Operator> listOperators(Location location) {
        List<Operator> ret = new ArrayList<>();

        if ((location == null) || (location.getId() == null)) {
            return ret;
        }

        try {
            List<OperatorRecord> records = operatorRepository.findBylocation_municipality_id(location.getId());
            if (records != null) {
                for (OperatorRecord record : records) {
                    Operator operator = get(record.id());
                    operator.setId(record.id());
                    operator.setName(record.name());
                    operator.setCallsign(record.callsign());
                    operator.setNTS(record.isNTS());
                    operator.setSkywarn(record.isSkywarn());
                    operator.setRACES(record.isRACES());
                    operator.setLocation(location);
                    ret.add(operator);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<Operator>() {
            @Override
            public int compare(Operator obj1, Operator obj2) {
                return obj1.getCallsign().compareTo(obj2.getCallsign());
            }
        });

        return ret;
    }

    public List<SectionOperator> listSections(Operator operator) {
        List<SectionOperator> ret = new ArrayList<>();

        if ((operator == null) || (operator.getId() == null)) {
            return ret;
        }

        try {
            List<OperatorSectionRecord> records = operatorsSectionRepository.findByoperatorId(operator.getId());
            if (records != null) {
                for (OperatorSectionRecord record : records) {
                    Section section = new Section();
                    section.setId(record.sectionId());
                    SectionOperator sectionOperator = new SectionOperator(operator, record.membershipType(), section);
                    ret.add(sectionOperator);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public List<SectionOperator> addOperator(Section section, Operator operator, MembershipType membershipType) {
        if ((section == null) || (section.getId() == null)) {
            return new ArrayList<>();
        }

        if ((operator == null) || (operator.getId() == null)) {
            return listOperators(section);
        }

        // determine if present
        boolean found = false;
        try {
            List<OperatorSectionRecord> records = operatorsSectionRepository.findBysectionId(section.getId());
            if (records != null) {
                for (OperatorSectionRecord record : records) {
                    if (record.operatorId().equals(operator.getId())) {
                        found = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        if (!found) {
            // add if not present
            try {
                OperatorSectionRecord record = new OperatorSectionRecord(UUID.randomUUID().toString(), section.getId(), operator.getId(), membershipType);
                operatorsSectionRepository.save(record);
            } catch (Exception e) {
                logger.error("Exception caught", e);
            }
        }

        return listOperators(section);
    }

    public List<SectionOperator> removeOperator(Section section, SectionOperator operator) {
        if ((section == null) || (section.getId() == null)) {
            return new ArrayList<>();
        }

        if ((operator == null) || (operator.getId() == null)) {
            return listOperators(section);
        }

        try {
            List<OperatorSectionRecord> records = operatorsSectionRepository.findBysectionId(section.getId());
            if (records != null) {
                for (OperatorSectionRecord record : records) {
                    if (record.operatorId().equals(operator.getId())) {
                        operatorsSectionRepository.delete(record);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return listOperators(section);
    }
}
