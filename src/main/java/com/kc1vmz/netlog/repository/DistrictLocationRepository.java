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
import com.kc1vmz.netlog.record.DistrictLocationRecord;


@Repository
@Transactional(readOnly = false)
public class DistrictLocationRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<DistrictLocationRecord> rowMapper = (rs, rowNumber) -> new DistrictLocationRecord(
                rs.getString("id"),
                rs.getString("section_id"),
                rs.getString("district_id"),
                rs.getString("location_id"));

    public DistrictLocationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DistrictLocationRecord> findAll() {
        return jdbc.query("SELECT * FROM district_location", rowMapper);
    }

    public Optional<DistrictLocationRecord> findById(String id) {
        return jdbc.query("SELECT * FROM district_location WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public DistrictLocationRecord save(DistrictLocationRecord record) {
        validate(record);
        jdbc.update("INSERT INTO district_location (id, section_id, district_id, location_id) "
                + "VALUES (?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.section_id()),
                JdbcValues.parameter(record.district_id()),
                JdbcValues.parameter(record.location_id()));
        return record;
    }

    public DistrictLocationRecord update(DistrictLocationRecord record) {
        validate(record);
        jdbc.update("UPDATE district_location SET section_id = ?, district_id = ?, location_id = ? WHERE id = ?",
                JdbcValues.parameter(record.section_id()),
                JdbcValues.parameter(record.district_id()),
                JdbcValues.parameter(record.location_id()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM district_location WHERE id = ?", id);
    }

    public void delete(DistrictLocationRecord record) {
        deleteById(record.id());
    }

    public List<DistrictLocationRecord> findBysection_id(String section_id) {
        return jdbc.query("SELECT * FROM district_location WHERE section_id = ?", rowMapper, section_id);
    }

    public List<DistrictLocationRecord> findBydistrict_id(String district_id) {
        return jdbc.query("SELECT * FROM district_location WHERE district_id = ?", rowMapper, district_id);
    }

    public List<DistrictLocationRecord> findBylocation_id(String location_id) {
        return jdbc.query("SELECT * FROM district_location WHERE location_id = ?", rowMapper, location_id);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(DistrictLocationRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.section_id(), "section_id");
        java.util.Objects.requireNonNull(record.district_id(), "district_id");
        java.util.Objects.requireNonNull(record.location_id(), "location_id");
    }
}
