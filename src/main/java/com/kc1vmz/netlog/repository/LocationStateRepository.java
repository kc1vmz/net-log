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
import com.kc1vmz.netlog.record.LocationStateRecord;


@Repository
@Transactional(readOnly = false)
public class LocationStateRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<LocationStateRecord> rowMapper = (rs, rowNumber) -> new LocationStateRecord(
                rs.getString("id"),
                rs.getString("country_id"),
                rs.getString("name"),
                rs.getString("abbreviation"));

    public LocationStateRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<LocationStateRecord> findAll() {
        return jdbc.query("SELECT * FROM location_state", rowMapper);
    }

    public Optional<LocationStateRecord> findById(String id) {
        return jdbc.query("SELECT * FROM location_state WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public LocationStateRecord save(LocationStateRecord record) {
        validate(record);
        jdbc.update("INSERT INTO location_state (id, country_id, name, abbreviation) "
                + "VALUES (?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.country_id()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.abbreviation()));
        return record;
    }

    public LocationStateRecord update(LocationStateRecord record) {
        validate(record);
        jdbc.update("UPDATE location_state SET country_id = ?, name = ?, abbreviation = ? WHERE id = ?",
                JdbcValues.parameter(record.country_id()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.abbreviation()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM location_state WHERE id = ?", id);
    }

    public void delete(LocationStateRecord record) {
        deleteById(record.id());
    }

    public List<LocationStateRecord> findBycountry_id(String country_id) {
        return jdbc.query("SELECT * FROM location_state WHERE country_id = ?", rowMapper, country_id);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(LocationStateRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.country_id(), "country_id");
        java.util.Objects.requireNonNull(record.name(), "name");
    }
}
