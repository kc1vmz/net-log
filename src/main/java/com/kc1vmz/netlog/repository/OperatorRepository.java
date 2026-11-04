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
import com.kc1vmz.netlog.record.OperatorRecord;


@Repository
@Transactional(readOnly = false)
public class OperatorRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<OperatorRecord> rowMapper = (rs, rowNumber) -> new OperatorRecord(
                rs.getString("id"),
                rs.getString("callsign"),
                rs.getString("name"),
                rs.getBoolean("is_nts"),
                rs.getBoolean("is_skywarn"),
                rs.getBoolean("is_races"),
                rs.getString("location_municipality_id"));

    public OperatorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OperatorRecord> findAll() {
        return jdbc.query("SELECT * FROM operator", rowMapper);
    }

    public Optional<OperatorRecord> findById(String id) {
        return jdbc.query("SELECT * FROM operator WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public OperatorRecord save(OperatorRecord record) {
        validate(record);
        jdbc.update("INSERT INTO operator (id, callsign, name, is_nts, is_skywarn, is_races, location_municipality_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.callsign()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.isNTS()),
                JdbcValues.parameter(record.isSkywarn()),
                JdbcValues.parameter(record.isRACES()),
                JdbcValues.parameter(record.location_municipality_id()));
        return record;
    }

    public OperatorRecord update(OperatorRecord record) {
        validate(record);
        jdbc.update("UPDATE operator SET callsign = ?, name = ?, is_nts = ?, is_skywarn = ?, is_races = ?, "
                + "location_municipality_id = ? WHERE id = ?",
                JdbcValues.parameter(record.callsign()),
                JdbcValues.parameter(record.name()),
                JdbcValues.parameter(record.isNTS()),
                JdbcValues.parameter(record.isSkywarn()),
                JdbcValues.parameter(record.isRACES()),
                JdbcValues.parameter(record.location_municipality_id()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM operator WHERE id = ?", id);
    }

    public void delete(OperatorRecord record) {
        deleteById(record.id());
    }

    public OperatorRecord findBycallsign(String callsign) {
        return jdbc.query("SELECT * FROM operator WHERE callsign = ?", rowMapper, callsign).stream().findFirst().orElse(null);
    }

    public List<OperatorRecord> findBylocation_municipality_id(String location_municipality_id) {
        return jdbc.query("SELECT * FROM operator WHERE location_municipality_id = ?", rowMapper, location_municipality_id);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(OperatorRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.callsign(), "callsign");
    }
}
