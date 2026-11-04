package com.kc1vmz.netlog.repository;

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

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.kc1vmz.netlog.record.ParticipantRecord;
import com.kc1vmz.netlog.enums.CommunicationBand;
import com.kc1vmz.netlog.enums.CommunicationPlatform;
import com.kc1vmz.netlog.enums.ElectricalPowerType;

@Repository
@Transactional(readOnly = false)
public class ParticipantRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<ParticipantRecord> rowMapper = (rs, rowNumber) -> new ParticipantRecord(
                rs.getString("id"),
                rs.getString("event_id"),
                rs.getString("operator_id"),
                JdbcValues.localDateTime(rs, "start_time"),
                JdbcValues.localDateTime(rs, "end_time"),
                JdbcValues.enumValue(rs, "primary_power", ElectricalPowerType.class),
                JdbcValues.enumValue(rs, "backup_power", ElectricalPowerType.class),
                rs.getInt("transmit_power"),
                JdbcValues.enumValue(rs, "communication_platform", CommunicationPlatform.class),
                JdbcValues.enumValue(rs, "communication_band", CommunicationBand.class));

    public ParticipantRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ParticipantRecord> findAll() {
        return jdbc.query("SELECT * FROM participant", rowMapper);
    }

    public Optional<ParticipantRecord> findById(String id) {
        return jdbc.query("SELECT * FROM participant WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public ParticipantRecord save(ParticipantRecord record) {
        validate(record);
        jdbc.update("INSERT INTO participant (id, event_id, operator_id, start_time, end_time, primary_power, "
                + "backup_power, transmit_power, communication_platform, communication_band) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.eventId()),
                JdbcValues.parameter(record.operatorId()),
                JdbcValues.parameter(record.startTime()),
                JdbcValues.parameter(record.endTime()),
                JdbcValues.parameter(record.primaryPower()),
                JdbcValues.parameter(record.backupPower()),
                JdbcValues.parameter(record.transmitPower()),
                JdbcValues.parameter(record.communicationPlatform()),
                JdbcValues.parameter(record.communicationBand()));
        return record;
    }

    public ParticipantRecord update(ParticipantRecord record) {
        validate(record);
        jdbc.update("UPDATE participant SET event_id = ?, operator_id = ?, start_time = ?, end_time = ?, "
                + "primary_power = ?, backup_power = ?, transmit_power = ?, communication_platform = ?, "
                + "communication_band = ? WHERE id = ?",
                JdbcValues.parameter(record.eventId()),
                JdbcValues.parameter(record.operatorId()),
                JdbcValues.parameter(record.startTime()),
                JdbcValues.parameter(record.endTime()),
                JdbcValues.parameter(record.primaryPower()),
                JdbcValues.parameter(record.backupPower()),
                JdbcValues.parameter(record.transmitPower()),
                JdbcValues.parameter(record.communicationPlatform()),
                JdbcValues.parameter(record.communicationBand()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM participant WHERE id = ?", id);
    }

    public void delete(ParticipantRecord record) {
        deleteById(record.id());
    }

    public List<ParticipantRecord> findByeventId(String id) {
        return jdbc.query("SELECT * FROM participant WHERE event_id = ?", rowMapper, id);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(ParticipantRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.eventId(), "eventId");
        java.util.Objects.requireNonNull(record.operatorId(), "operatorId");
        java.util.Objects.requireNonNull(record.primaryPower(), "primaryPower");
        java.util.Objects.requireNonNull(record.backupPower(), "backupPower");
        java.util.Objects.requireNonNull(record.communicationPlatform(), "communicationPlatform");
        java.util.Objects.requireNonNull(record.communicationBand(), "communicationBand");
    }
}
