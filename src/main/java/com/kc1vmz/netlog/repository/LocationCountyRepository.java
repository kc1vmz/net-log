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
import com.kc1vmz.netlog.record.LocationCountyRecord;


@Repository
@Transactional(readOnly = false)
public class LocationCountyRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<LocationCountyRecord> rowMapper = (rs, rowNumber) -> new LocationCountyRecord(
                rs.getString("id"),
                rs.getString("country_id"),
                rs.getString("state_id"),
                rs.getString("name"));

    public LocationCountyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<LocationCountyRecord> findAll() {
        return jdbc.query("SELECT * FROM location_county", rowMapper);
    }

    public Optional<LocationCountyRecord> findById(String id) {
        return jdbc.query("SELECT * FROM location_county WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public LocationCountyRecord save(LocationCountyRecord record) {
        validate(record);
        jdbc.update("INSERT INTO location_county (id, country_id, state_id, name) "
                + "VALUES (?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.country_id()),
                JdbcValues.parameter(record.state_id()),
                JdbcValues.parameter(record.name()));
        return record;
    }

    public LocationCountyRecord update(LocationCountyRecord record) {
        validate(record);
        jdbc.update("UPDATE location_county SET country_id = ?, state_id = ?, name = ? WHERE id = ?",
                JdbcValues.parameter(record.country_id()),
                JdbcValues.parameter(record.state_id()),
                JdbcValues.parameter(record.name()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM location_county WHERE id = ?", id);
    }

    public void delete(LocationCountyRecord record) {
        deleteById(record.id());
    }

    public List<LocationCountyRecord> findBystate_id(String state_id) {
        return jdbc.query("SELECT * FROM location_county WHERE state_id = ?", rowMapper, state_id);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(LocationCountyRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.country_id(), "country_id");
        java.util.Objects.requireNonNull(record.state_id(), "state_id");
        java.util.Objects.requireNonNull(record.name(), "name");
    }
}
