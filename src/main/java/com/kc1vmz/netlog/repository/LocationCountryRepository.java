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
import com.kc1vmz.netlog.record.LocationCountryRecord;


@Repository
@Transactional(readOnly = false)
public class LocationCountryRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<LocationCountryRecord> rowMapper = (rs, rowNumber) -> new LocationCountryRecord(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("abbreviation"));

    public LocationCountryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<LocationCountryRecord> findAll() {
        return jdbc.query("SELECT * FROM location_country", rowMapper);
    }

    public Optional<LocationCountryRecord> findById(String id) {
        return jdbc.query("SELECT * FROM location_country WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public LocationCountryRecord save(LocationCountryRecord record) {
        validate(record);
        jdbc.update("INSERT INTO location_country (id, name, abbreviation) "
                + "VALUES (?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.abbreviation()));
        return record;
    }

    public LocationCountryRecord update(LocationCountryRecord record) {
        validate(record);
        jdbc.update("UPDATE location_country SET name = ?, abbreviation = ? WHERE id = ?",
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.abbreviation()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM location_country WHERE id = ?", id);
    }

    public void delete(LocationCountryRecord record) {
        deleteById(record.id());
    }

    public Optional<LocationCountryRecord> findByname(String name) {
        return jdbc.query("SELECT * FROM location_country WHERE name = ?", rowMapper, name).stream().findFirst();
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(LocationCountryRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.name(), "name");
    }
}
