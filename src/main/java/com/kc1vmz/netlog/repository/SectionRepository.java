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
import com.kc1vmz.netlog.record.SectionRecord;


@Repository
@Transactional(readOnly = false)
public class SectionRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<SectionRecord> rowMapper = (rs, rowNumber) -> new SectionRecord(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBoolean("active"),
                rs.getString("leader_name"),
                rs.getString("leader_contact"));

    public SectionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SectionRecord> findAll() {
        return jdbc.query("SELECT * FROM section", rowMapper);
    }

    public Optional<SectionRecord> findById(String id) {
        return jdbc.query("SELECT * FROM section WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public SectionRecord save(SectionRecord record) {
        validate(record);
        jdbc.update("INSERT INTO section (id, name, description, active, leader_name, leader_contact) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.description()),
                JdbcValues.parameter(record.active()),
                JdbcValues.parameter(record.leaderName()),
                JdbcValues.parameter(record.leaderContact()));
        return record;
    }

    public SectionRecord update(SectionRecord record) {
        validate(record);
        jdbc.update("UPDATE section SET name = ?, description = ?, active = ?, leader_name = ?, leader_contact = ? WHERE id = ?",
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.description()),
                JdbcValues.parameter(record.active()),
                JdbcValues.parameter(record.leaderName()),
                JdbcValues.parameter(record.leaderContact()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM section WHERE id = ?", id);
    }

    public void delete(SectionRecord record) {
        deleteById(record.id());
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(SectionRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.name(), "name");
        java.util.Objects.requireNonNull(record.description(), "description");
    }
}
