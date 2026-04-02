package com.kc1vmz.netlog.accessor;

import java.time.LocalDateTime;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.enums.ElectricalPowerType;
import com.kc1vmz.netlog.object.Event;
import com.kc1vmz.netlog.object.Operator;
import com.kc1vmz.netlog.object.Participant;
import com.kc1vmz.netlog.record.ParticipantRecord;
import com.kc1vmz.netlog.repository.ParticipantRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ParticipantAccessor {
    @Inject
    private ParticipantRepository participantRepository;
    @Inject
    private OperatorAccessor operatorAccessor;
    private static final Logger logger = LogManager.getLogger(ParticipantAccessor.class);

    public Participant get(String id) {
        Participant ret = null;

        try {
            Optional<ParticipantRecord> recordOpt = participantRepository.findById(id);
            if (recordOpt.isPresent()) {
                ParticipantRecord record = recordOpt.get();
                ret = new Participant();
                ret.setBackupPower(record.backupPower());
                ret.setEndTime(record.endTime());

                Event event = new Event();
                event.setId(record.eventId());
                ret.setEvent(event);

                ret.setId(record.id());

                ret.setOperator(operatorAccessor.get(record.operatorId()));

                ret.setPrimaryPower(record.primaryPower());
                ret.setStartTime(record.startTime());
                ret.setTransmitPower(record.transmitPower());
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public Participant update(String id, Participant obj) {
        try {
            Optional<ParticipantRecord> recordOpt =  participantRepository.findById(id);
            if (!recordOpt.isPresent()) {
                return null;
            }

            ParticipantRecord rec = recordOpt.get();
            ParticipantRecord recNew = new ParticipantRecord(rec.id(), rec.eventId(), rec.operatorId(), obj.getStartTime(), obj.getEndTime(), 
                                    obj.getPrimaryPower(), obj.getBackupPower(), obj.getTransmitPower());
            participantRepository.update(recNew);
            obj.setId(recNew.id());
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return null;
    }

    public List<Participant> listParticipants(Event event, boolean checkedInOnly) {
        List<Participant> ret = new ArrayList<>();
        try {
            List<ParticipantRecord> records = participantRepository.findByeventId(event.getId());
            if (records != null) {
                for (ParticipantRecord record : records) {
                    if (checkedInOnly) {
                        if (record.endTime() != null) {
                            continue;
                        }
                    }
                    Participant participant = new Participant();
                    participant.setBackupPower(record.backupPower());
                    participant.setEndTime(record.endTime());
                    participant.setEvent(event);
                    participant.setId(record.id());

                    participant.setOperator(operatorAccessor.get(record.operatorId()));

                    participant.setPrimaryPower(record.primaryPower());
                    participant.setStartTime(record.startTime());
                    participant.setTransmitPower(record.transmitPower());
                    ret.add(participant);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return ret;
    }

    public List<Participant> checkInParticipant(Event event, Operator operator, LocalDateTime checkInTime) {
        if ((event == null) || (event.getId() == null)) {
            return new ArrayList<>();
        }

        if ((operator == null) || (operator.getId() == null)) {
            return listParticipants(event, true);
        }

        // determine if present
        boolean found = false;
        try {
            List<ParticipantRecord> records = participantRepository.findByeventId(event.getId());
            if (records != null) {
                for (ParticipantRecord record : records) {
                    if (record.operatorId().equals(operator.getId()) && (record.endTime() == null)) {
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
                ParticipantRecord record = new ParticipantRecord(UUID.randomUUID().toString(), event.getId(), operator.getId(), checkInTime, null, 
                                        ElectricalPowerType.UNKNOWN, ElectricalPowerType.UNKNOWN, 0);
                participantRepository.save(record);
            } catch (Exception e) {
                logger.error("Exception caught", e);
            }
        }

        return listParticipants(event, true);
    }

    public List<Participant> checkOutParticipant(Event event, Operator operator, LocalDateTime checkOutTime) {
        if ((event == null) || (event.getId() == null)) {
            return new ArrayList<>();
        }

        if ((operator == null) || (operator.getId() == null)) {
            return listParticipants(event, true);
        }

        try {
            List<ParticipantRecord> records = participantRepository.findByeventId(event.getId());
            if (records != null) {
                for (ParticipantRecord record : records) {
                    if (record.operatorId().equals(operator.getId()) && (record.endTime() == null)) {
                        // add if not present
                        ParticipantRecord recordUpdated = new ParticipantRecord(record.id(),record.eventId(), record.operatorId(), record.startTime(), checkOutTime, 
                                                record.primaryPower(), record.backupPower(), record.transmitPower());
                        participantRepository.update(recordUpdated);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        return listParticipants(event, true);
    }

    public void delete(String id) {
        participantRepository.deleteById(id);
    }
}
