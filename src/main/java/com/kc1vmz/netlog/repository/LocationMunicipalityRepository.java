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
import com.kc1vmz.netlog.record.LocationMunicipalityRecord;


@Repository
@Transactional(readOnly = false)
public class LocationMunicipalityRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<LocationMunicipalityRecord> rowMapper = (rs, rowNumber) -> new LocationMunicipalityRecord(
                rs.getString("id"),
                rs.getString("country_id"),
                rs.getString("state_id"),
                rs.getString("county_id"),
                rs.getString("name"));

    public LocationMunicipalityRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<LocationMunicipalityRecord> findAll() {
        return jdbc.query("SELECT * FROM location_municipality", rowMapper);
    }

    public Optional<LocationMunicipalityRecord> findById(String id) {
        return jdbc.query("SELECT * FROM location_municipality WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public LocationMunicipalityRecord save(LocationMunicipalityRecord record) {
        validate(record);
        jdbc.update("INSERT INTO location_municipality (id, country_id, state_id, county_id, name) "
                + "VALUES (?, ?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.country_id()),
                JdbcValues.parameter(record.state_id()),
                JdbcValues.parameter(record.county_id()),
                JdbcValues.parameter(record.name()));
        return record;
    }

    public LocationMunicipalityRecord update(LocationMunicipalityRecord record) {
        validate(record);
        jdbc.update("UPDATE location_municipality SET country_id = ?, state_id = ?, county_id = ?, name = ? WHERE id = ?",
                JdbcValues.parameter(record.country_id()),
                JdbcValues.parameter(record.state_id()),
                JdbcValues.parameter(record.county_id()),
                JdbcValues.parameter(record.name()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM location_municipality WHERE id = ?", id);
    }

    public void delete(LocationMunicipalityRecord record) {
        deleteById(record.id());
    }

    public List<LocationMunicipalityRecord> findBystate_id(String state_id) {
        return jdbc.query("SELECT * FROM location_municipality WHERE state_id = ?", rowMapper, state_id);
    }

    public List<LocationMunicipalityRecord> findBycounty_id(String county_id) {
        return jdbc.query("SELECT * FROM location_municipality WHERE county_id = ?", rowMapper, county_id);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(LocationMunicipalityRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.country_id(), "country_id");
        java.util.Objects.requireNonNull(record.name(), "name");
    }
}
