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
import com.kc1vmz.netlog.record.EventRecord;
import com.kc1vmz.netlog.enums.EventType;

@Repository
@Transactional(readOnly = false)
public class EventRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<EventRecord> rowMapper = (rs, rowNumber) -> new EventRecord(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("recurring_event_id"),
                rs.getString("section_id"),
                JdbcValues.enumValue(rs, "type", EventType.class),
                rs.getString("location"),
                JdbcValues.localDateTime(rs, "start_time"),
                JdbcValues.localDateTime(rs, "end_time"),
                rs.getBoolean("secure"),
                rs.getString("net_control_callsign"),
                rs.getBoolean("active_section"),
                rs.getInt("state"));

    public EventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<EventRecord> findAll() {
        return jdbc.query("SELECT * FROM event", rowMapper);
    }

    public Optional<EventRecord> findById(String id) {
        return jdbc.query("SELECT * FROM event WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public EventRecord save(EventRecord record) {
        validate(record);
        jdbc.update("INSERT INTO event (id, name, description, recurring_event_id, section_id, type, location, "
                + "start_time, end_time, secure, net_control_callsign, active_section, state) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.description()),
                JdbcValues.parameter(record.recurringEventId()),
                JdbcValues.parameter(record.sectionId()),
                JdbcValues.parameter(record.type()),
                JdbcValues.parameter(record.location()),
                JdbcValues.parameter(record.startTime()),
                JdbcValues.parameter(record.endTime()),
                JdbcValues.parameter(record.secure()),
                JdbcValues.parameter(record.netControlCallsign()),
                JdbcValues.parameter(record.activeSection()),
                JdbcValues.parameter(record.state()));
        return record;
    }

    public EventRecord update(EventRecord record) {
        validate(record);
        jdbc.update("UPDATE event SET name = ?, description = ?, recurring_event_id = ?, section_id = ?, type = ?, "
                + "location = ?, start_time = ?, end_time = ?, secure = ?, net_control_callsign = ?, "
                + "active_section = ?, state = ? WHERE id = ?",
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.description()),
                JdbcValues.parameter(record.recurringEventId()),
                JdbcValues.parameter(record.sectionId()),
                JdbcValues.parameter(record.type()),
                JdbcValues.parameter(record.location()),
                JdbcValues.parameter(record.startTime()),
                JdbcValues.parameter(record.endTime()),
                JdbcValues.parameter(record.secure()),
                JdbcValues.parameter(record.netControlCallsign()),
                JdbcValues.parameter(record.activeSection()),
                JdbcValues.parameter(record.state()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM event WHERE id = ?", id);
    }

    public void delete(EventRecord record) {
        deleteById(record.id());
    }

    public List<EventRecord> findBysectionId(String sectionId) {
        return jdbc.query("SELECT * FROM event WHERE section_id = ?", rowMapper, sectionId);
    }

    public List<EventRecord> findByrecurringEventId(String recurringEventId) {
        return jdbc.query("SELECT * FROM event WHERE recurring_event_id = ?", rowMapper, recurringEventId);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(EventRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.name(), "name");
        java.util.Objects.requireNonNull(record.recurringEventId(), "recurringEventId");
        java.util.Objects.requireNonNull(record.sectionId(), "sectionId");
        java.util.Objects.requireNonNull(record.type(), "type");
        java.util.Objects.requireNonNull(record.startTime(), "startTime");
    }
}
