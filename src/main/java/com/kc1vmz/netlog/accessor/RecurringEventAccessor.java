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

import com.kc1vmz.netlog.object.RecurringEvent;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.record.RecurringEventRecord;
import com.kc1vmz.netlog.repository.RecurringEventRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RecurringEventAccessor {
    @Inject
    private RecurringEventRepository recurringEventRepository;
    private static final Logger logger = LogManager.getLogger(RecurringEventAccessor.class);

    public RecurringEvent get(String id) {
        RecurringEvent ret = null;

        try {
            Optional<RecurringEventRecord> recordOpt =  recurringEventRepository.findById(id);
            if (recordOpt.isPresent()) {
                RecurringEventRecord record = recordOpt.get();
                ret = new RecurringEvent();
                ret.setId(record.id());
                ret.setName(record.name());
                Section section = new Section();
                section.setId(record.sectionId());
                ret.setSection(section);
                ret.setDescription(record.description());
                ret.setType(record.type());
                ret.setSchedule(record.schedule());
                ret.setLocation(record.location());
                ret.setNetControlCallsign(record.netControlCallsign());
                ret.setSectionActive(record.activeSection());
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public void delete(String id) {
        recurringEventRepository.deleteById(id);
    }

    public RecurringEvent update(String id, RecurringEvent obj) {
        try {
            Optional<RecurringEventRecord> recordOpt =  recurringEventRepository.findById(id);
            if (!recordOpt.isPresent()) {
                return null;
            }

            RecurringEventRecord rec = recordOpt.get();
            RecurringEventRecord recNew = new RecurringEventRecord(rec.id(), rec.sectionId(), obj.getName(), obj.getDescription(),
                                                     obj.getType(), obj.getSchedule(), obj.getLocation(), obj.getNetControlCallsign(), obj.isSectionActive());
            recurringEventRepository.update(recNew);
            obj.setId(id);
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return null;
    }

    public List<RecurringEvent> listBySection(Section section, boolean sectionActiveOnly) {
        List<RecurringEvent> ret = new ArrayList<>();

        if ((section == null) || (section.getId() == null)) {
            return ret;
        }

        try {
            List<RecurringEventRecord> records =  recurringEventRepository.findBysectionId(section.getId());
            if (records != null) {
                for (RecurringEventRecord record : records) {
                    if (sectionActiveOnly && !record.activeSection()) {
                        continue;
                    }
                    RecurringEvent recurringEvent = new RecurringEvent();
                    recurringEvent.setId(record.id());
                    recurringEvent.setName(record.name());
                    recurringEvent.setDescription(record.description());
                    recurringEvent.setType(record.type());
                    recurringEvent.setSchedule(record.schedule());
                    recurringEvent.setLocation(record.location());
                    recurringEvent.setSection(section);
                    recurringEvent.setNetControlCallsign(record.netControlCallsign());
                    recurringEvent.setSectionActive(record.activeSection());
                    ret.add(recurringEvent);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<RecurringEvent>() {
            @Override
            public int compare(RecurringEvent obj1, RecurringEvent obj2) {
                    String s1 = obj1.getName();
                    String s2 = obj2.getName();
                    return s1.compareTo(s2);
            }
        });
        return ret;
    }

    public List<RecurringEvent> list(boolean sectionActiveOnly) {
        List<RecurringEvent> ret = new ArrayList<>();

        try {
            List<RecurringEventRecord> records =  recurringEventRepository.findAll();
            if (records != null) {
                for (RecurringEventRecord record : records) {
                    if (sectionActiveOnly && !record.activeSection()) {
                        continue;
                    }
                    RecurringEvent recurringEvent = new RecurringEvent();
                    recurringEvent.setId(record.id());
                    recurringEvent.setName(record.name());
                    recurringEvent.setDescription(record.description());
                    recurringEvent.setType(record.type());
                    recurringEvent.setSchedule(record.schedule());
                    recurringEvent.setLocation(record.location());
                    recurringEvent.setNetControlCallsign(record.netControlCallsign());
                    recurringEvent.setSectionActive(record.activeSection());

                    Section section = new Section();
                    section.setId(record.sectionId());
                    recurringEvent.setSection(section);

                    ret.add(recurringEvent);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<RecurringEvent>() {
            @Override
            public int compare(RecurringEvent obj1, RecurringEvent obj2) {
                    String s1 = obj1.getName();
                    String s2 = obj2.getName();
                    return s1.compareTo(s2);
            }
        });
        return ret;
    }

    public RecurringEvent createBySection(Section section, RecurringEvent recurringEvent) {
        if ((section == null) || (section.getId() == null)) {
            return null;
        }

        try {
            RecurringEventRecord rec = new RecurringEventRecord(UUID.randomUUID().toString(), section.getId(), recurringEvent.getName(),
                                                recurringEvent.getDescription(), recurringEvent.getType(), recurringEvent.getSchedule(), 
                                                recurringEvent.getLocation(), recurringEvent.getNetControlCallsign(), section.isActive());
            RecurringEventRecord recNew = recurringEventRepository.save(rec);
            recurringEvent.setId(recNew.id());
            return recurringEvent;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return null;
    }
}
