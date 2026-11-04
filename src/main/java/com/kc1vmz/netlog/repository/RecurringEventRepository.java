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
import com.kc1vmz.netlog.record.RecurringEventRecord;
import com.kc1vmz.netlog.enums.EventType;

@Repository
@Transactional(readOnly = false)
public class RecurringEventRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<RecurringEventRecord> rowMapper = (rs, rowNumber) -> new RecurringEventRecord(
                rs.getString("id"),
                rs.getString("section_id"),
                rs.getString("name"),
                rs.getString("description"),
                JdbcValues.enumValue(rs, "type", EventType.class),
                rs.getString("schedule"),
                rs.getString("location"),
                rs.getString("net_control_callsign"),
                rs.getBoolean("active_section"));

    public RecurringEventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<RecurringEventRecord> findAll() {
        return jdbc.query("SELECT * FROM recurring_event", rowMapper);
    }

    public Optional<RecurringEventRecord> findById(String id) {
        return jdbc.query("SELECT * FROM recurring_event WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public RecurringEventRecord save(RecurringEventRecord record) {
        validate(record);
        jdbc.update("INSERT INTO recurring_event (id, section_id, name, description, type, schedule, location, "
                + "net_control_callsign, active_section) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.sectionId()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.description()),
                JdbcValues.parameter(record.type()),
                JdbcValues.parameter(record.schedule()),
                JdbcValues.parameter(record.location()),
                JdbcValues.parameter(record.netControlCallsign()),
                JdbcValues.parameter(record.activeSection()));
        return record;
    }

    public RecurringEventRecord update(RecurringEventRecord record) {
        validate(record);
        jdbc.update("UPDATE recurring_event SET section_id = ?, name = ?, description = ?, type = ?, schedule = ?, "
                + "location = ?, net_control_callsign = ?, active_section = ? WHERE id = ?",
                JdbcValues.parameter(record.sectionId()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.description()),
                JdbcValues.parameter(record.type()),
                JdbcValues.parameter(record.schedule()),
                JdbcValues.parameter(record.location()),
                JdbcValues.parameter(record.netControlCallsign()),
                JdbcValues.parameter(record.activeSection()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM recurring_event WHERE id = ?", id);
    }

    public void delete(RecurringEventRecord record) {
        deleteById(record.id());
    }

    public List<RecurringEventRecord> findBysectionId(String sectionId) {
        return jdbc.query("SELECT * FROM recurring_event WHERE section_id = ?", rowMapper, sectionId);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(RecurringEventRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.sectionId(), "sectionId");
        java.util.Objects.requireNonNull(record.name(), "name");
        java.util.Objects.requireNonNull(record.description(), "description");
        java.util.Objects.requireNonNull(record.type(), "type");
        java.util.Objects.requireNonNull(record.schedule(), "schedule");
        java.util.Objects.requireNonNull(record.location(), "location");
    }
}
