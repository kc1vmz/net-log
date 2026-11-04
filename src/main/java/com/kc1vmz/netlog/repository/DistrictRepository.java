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
import com.kc1vmz.netlog.record.DistrictRecord;


@Repository
@Transactional(readOnly = false)
public class DistrictRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<DistrictRecord> rowMapper = (rs, rowNumber) -> new DistrictRecord(
                rs.getString("id"),
                rs.getString("section_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("leader_name"),
                rs.getString("leader_contact"));

    public DistrictRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DistrictRecord> findAll() {
        return jdbc.query("SELECT * FROM district", rowMapper);
    }

    public Optional<DistrictRecord> findById(String id) {
        return jdbc.query("SELECT * FROM district WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public DistrictRecord save(DistrictRecord record) {
        validate(record);
        jdbc.update("INSERT INTO district (id, section_id, name, description, leader_name, leader_contact) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.section_id()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.description()),
                JdbcValues.parameter(record.leader_name()),
                JdbcValues.parameter(record.leader_contact()));
        return record;
    }

    public DistrictRecord update(DistrictRecord record) {
        validate(record);
        jdbc.update("UPDATE district SET section_id = ?, name = ?, description = ?, leader_name = ?, "
                + "leader_contact = ? WHERE id = ?",
                JdbcValues.parameter(record.section_id()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.description()),
                JdbcValues.parameter(record.leader_name()),
                JdbcValues.parameter(record.leader_contact()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM district WHERE id = ?", id);
    }

    public void delete(DistrictRecord record) {
        deleteById(record.id());
    }

    public List<DistrictRecord> findBysection_id(String section_id) {
        return jdbc.query("SELECT * FROM district WHERE section_id = ?", rowMapper, section_id);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(DistrictRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.section_id(), "section_id");
        java.util.Objects.requireNonNull(record.name(), "name");
    }
}
