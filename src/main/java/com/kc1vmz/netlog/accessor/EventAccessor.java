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

import com.kc1vmz.netlog.enums.EventState;
import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.RecurringEvent;
import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.record.EventRecord;
import com.kc1vmz.netlog.repository.EventRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class EventAccessor {
    @Inject
    private EventRepository eventRepository;
    private static final Logger logger = LogManager.getLogger(EventAccessor.class);

    public Event get(String id) {
        Event ret = null;

        try {
            Optional<EventRecord> recordOpt = eventRepository.findById(id);
            if (recordOpt.isPresent()) {
                EventRecord record = recordOpt.get();
                ret = new Event();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setDescription(record.description());
                ret.setType(record.type());
                ret.setStartTime(record.startTime());
                ret.setEndTime(record.endTime());
                ret.setLocation(record.location());
                ret.setNetControlCallsign(record.netControlCallsign());
                ret.setSectionActive(record.activeSection());
                ret.setState(EventState.values()[record.state()]);

                RecurringEvent recurringEvent = new RecurringEvent();
                recurringEvent.setId(record.recurringEventId());
                ret.setRecurringEvent(recurringEvent);

                Section section = new Section();
                section.setId(record.sectionId());
                ret.setSection(section);
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public void delete(String id) {
        eventRepository.deleteById(id);
    }

    public Event update(String id, Event obj) {
        try {
            Optional<EventRecord> recordOpt =  eventRepository.findById(id);
            if (!recordOpt.isPresent()) {
                return null;
            }

            EventRecord rec = recordOpt.get();
            EventRecord recNew = new EventRecord(rec.id(), obj.getName(), obj.getDescription(),
                                                    rec.recurringEventId(),  rec.sectionId(), rec.type(), obj.getLocation(), obj.getStartTime(), 
                                                    obj.getEndTime(),
                                                    (obj.getState().equals(EventState.SECURE) || obj.getState().equals(EventState.NOT_HELD)) ? true : false, 
                                                    obj.getNetControlCallsign(), obj.isSectionActive(), obj.getState().ordinal());
                                                    eventRepository.update(recNew);
            obj.setId(recNew.id());
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return null;
    }

    public List<Event> listBySection(Section section, boolean activeOnly, boolean sectionActiveOnly) {
        List<Event> ret = new ArrayList<>();

        if ((section == null) || (section.getId() == null)) {
            return ret;
        }

        try {
            List<EventRecord> records =  eventRepository.findBysectionId(section.getId());
            if (records != null) {
                for (EventRecord record : records) {
                    if (sectionActiveOnly && !record.activeSection()) {
                        continue;
                    }
                    if (activeOnly && ((record.state() == EventState.SECURE.ordinal()) || (record.state() == EventState.NOT_HELD.ordinal()))) {
                        continue;
                    }
                    Event event = new Event();
                    event.setId(record.id());
                    event.setName(record.name());
                    event.setDescription(record.description());
                    event.setType(record.type());
                    event.setStartTime(record.startTime());
                    event.setEndTime(record.endTime());
                    event.setLocation(record.location());
                    event.setState(EventState.values()[record.state()]);
                    event.setNetControlCallsign(record.netControlCallsign());
                    event.setSectionActive(record.activeSection());

                    RecurringEvent recurringEvent = new RecurringEvent();
                    recurringEvent.setId(record.recurringEventId());
                    event.setRecurringEvent(recurringEvent);

                    event.setSection(section);

                    ret.add(event);
                }

                Collections.sort(ret, new Comparator<Event>() {
                    @Override
                    public int compare(Event obj1, Event obj2) {
                        String s1 = obj1.getPrettyStartTime()+obj1.getName();
                        String s2 = obj2.getPrettyStartTime()+obj2.getName();
                        return s1.compareTo(s2);
                    }
                });

            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public List<Event> listByRecurringEvent(RecurringEvent recurringEvent, boolean activeOnly, boolean sectionActiveOnly) {
        List<Event> ret = new ArrayList<>();

        if ((recurringEvent == null) || (recurringEvent.getId() == null)) {
            return ret;
        }

        try {
            List<EventRecord> records =  eventRepository.findByrecurringEventId(recurringEvent.getId());
            if (records != null) {
                for (EventRecord record : records) {
                    if (sectionActiveOnly && !record.activeSection()) {
                        continue;
                    }
                    if (activeOnly && ((record.state() == EventState.SECURE.ordinal()) || (record.state() == EventState.NOT_HELD.ordinal()))) {
                        continue;
                    }
                    Event event = new Event();
                    event.setId(record.id());
                    event.setName(record.name());
                    event.setDescription(record.description());
                    event.setType(record.type());
                    event.setStartTime(record.startTime());
                    event.setEndTime(record.endTime());
                    event.setLocation(record.location());
                    event.setState(EventState.values()[record.state()]);
                    event.setNetControlCallsign(record.netControlCallsign());
                    event.setSectionActive(record.activeSection());

                    event.setRecurringEvent(recurringEvent);

                    Section section = new Section();
                    section.setId(record.sectionId());
                    event.setSection(section);

                    ret.add(event);
                }
            }

            Collections.sort(ret, new Comparator<Event>() {
                @Override
                public int compare(Event obj1, Event obj2) {
                        String s1 = obj1.getPrettyStartTime()+obj1.getName();
                        String s2 = obj2.getPrettyStartTime()+obj2.getName();
                        return s1.compareTo(s2);
                }
            });

        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public List<Event> list(boolean activeOnly, boolean sectionActiveOnly) {
        List<Event> ret = new ArrayList<>();

        try {
            List<EventRecord> records =  eventRepository.findAll();
            if (records != null) {
                for (EventRecord record : records) {
                    if (sectionActiveOnly && !record.activeSection()) {
                        continue;
                    }
                    if (activeOnly && ((record.state() == EventState.SECURE.ordinal()) || (record.state() == EventState.NOT_HELD.ordinal()))) {
                        continue;
                    }
                    Event event = new Event();
                    event.setId(record.id());
                    event.setName(record.name());
                    event.setDescription(record.description());
                    event.setType(record.type());
                    event.setStartTime(record.startTime());
                    event.setEndTime(record.endTime());
                    event.setLocation(record.location());
                    event.setState(EventState.values()[record.state()]);
                    event.setNetControlCallsign(record.netControlCallsign());
                    event.setSectionActive(record.activeSection());

                    RecurringEvent recurringEvent = new RecurringEvent();
                    recurringEvent.setId(record.recurringEventId());
                    event.setRecurringEvent(recurringEvent);

                    Section section = new Section();
                    section.setId(record.sectionId());
                    event.setSection(section);

                    ret.add(event);
                }
            }

            Collections.sort(ret, new Comparator<Event>() {
                @Override
                public int compare(Event obj1, Event obj2) {
                        String s1 = obj1.getPrettyStartTime()+obj1.getName();
                        String s2 = obj2.getPrettyStartTime()+obj2.getName();
                        return s1.compareTo(s2);
                }
            });

        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public List<Event> listSecured(Section sectionSource) {
        List<Event> ret = new ArrayList<>();

        try {
            List<EventRecord> records;
            if (sectionSource == null) {
                records = eventRepository.findAll();
            } else {
                records = eventRepository.findBysectionId(sectionSource.getId());
            }

            if (records != null) {
                for (EventRecord record : records) {
                    if (!(record.state() == EventState.SECURE.ordinal()) && !(record.state() == EventState.NOT_HELD.ordinal())) {
                        continue;
                    }
                    Event event = new Event();
                    event.setId(record.id());
                    event.setName(record.name());
                    event.setDescription(record.description());
                    event.setType(record.type());
                    event.setStartTime(record.startTime());
                    event.setEndTime(record.endTime());
                    event.setLocation(record.location());
                    event.setState(EventState.values()[record.state()]);
                    event.setNetControlCallsign(record.netControlCallsign());
                    event.setSectionActive(record.activeSection());

                    RecurringEvent recurringEvent = new RecurringEvent();
                    recurringEvent.setId(record.recurringEventId());
                    event.setRecurringEvent(recurringEvent);

                    if (sectionSource == null) {
                        Section section = new Section();
                        section.setId(record.sectionId());
                        event.setSection(section);
                    } else {
                        event.setSection(sectionSource);
                    }

                    ret.add(event);
                }
            }
            Collections.sort(ret, new Comparator<Event>() {
                @Override
                public int compare(Event obj1, Event obj2) {
                        String s1 = obj1.getPrettyStartTime()+obj1.getName();
                        String s2 = obj2.getPrettyStartTime()+obj2.getName();
                        return s1.compareTo(s2);
                }
            });
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public Event create(Section section, RecurringEvent recurringEvent, Event event) {
        if ((section == null) || (section.getId() == null)) {
            return null;
        }
        if ((recurringEvent == null) || (recurringEvent.getId() == null)) {
            return null;
        }

        try {
            EventRecord rec = new EventRecord(UUID.randomUUID().toString(), event.getName(), event.getDescription(), recurringEvent.getId(), section.getId(),
                                        event.getType(), event.getLocation(), event.getStartTime(), event.getEndTime(), false, event.getNetControlCallsign(), 
                                        event.isSectionActive(), event.getState().ordinal());
            EventRecord recNew = eventRepository.save(rec);
            event.setId(recNew.id());
            event.setSection(section);
            event.setRecurringEvent(recurringEvent);
            return event;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return null;
    }
}
