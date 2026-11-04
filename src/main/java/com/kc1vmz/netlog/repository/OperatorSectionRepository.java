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
import com.kc1vmz.netlog.record.OperatorSectionRecord;
import com.kc1vmz.netlog.enums.MembershipType;

@Repository
@Transactional(readOnly = false)
public class OperatorSectionRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<OperatorSectionRecord> rowMapper = (rs, rowNumber) -> new OperatorSectionRecord(
                rs.getString("id"),
                rs.getString("section_id"),
                rs.getString("operator_id"),
                JdbcValues.enumValue(rs, "membership_type", MembershipType.class));

    public OperatorSectionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OperatorSectionRecord> findAll() {
        return jdbc.query("SELECT * FROM operator_section", rowMapper);
    }

    public Optional<OperatorSectionRecord> findById(String id) {
        return jdbc.query("SELECT * FROM operator_section WHERE id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public OperatorSectionRecord save(OperatorSectionRecord record) {
        validate(record);
        jdbc.update("INSERT INTO operator_section (id, section_id, operator_id, membership_type) "
                + "VALUES (?, ?, ?, ?)",
                JdbcValues.parameter(record.id()),
                JdbcValues.parameter(record.sectionId()),
                JdbcValues.parameter(record.operatorId()),
                JdbcValues.parameter(record.membershipType()));
        return record;
    }

    public OperatorSectionRecord update(OperatorSectionRecord record) {
        validate(record);
        jdbc.update("UPDATE operator_section SET section_id = ?, operator_id = ?, membership_type = ? WHERE id = ?",
                JdbcValues.parameter(record.sectionId()),
                JdbcValues.parameter(record.operatorId()),
                JdbcValues.parameter(record.membershipType()),
                record.id());
        return record;
    }

    public void deleteById(String id) {
        jdbc.update("DELETE FROM operator_section WHERE id = ?", id);
    }

    public void delete(OperatorSectionRecord record) {
        deleteById(record.id());
    }

    public List<OperatorSectionRecord> findBysectionId(String sectionId) {
        return jdbc.query("SELECT * FROM operator_section WHERE section_id = ?", rowMapper, sectionId);
    }

    public List<OperatorSectionRecord> findByoperatorId(String operatorId) {
        return jdbc.query("SELECT * FROM operator_section WHERE operator_id = ?", rowMapper, operatorId);
    }

    // Retain Micronaut Data's implicit non-null contract for unannotated record components.
    private void validate(OperatorSectionRecord record) {
        java.util.Objects.requireNonNull(record.id(), "id");
        java.util.Objects.requireNonNull(record.sectionId(), "sectionId");
        java.util.Objects.requireNonNull(record.operatorId(), "operatorId");
        java.util.Objects.requireNonNull(record.membershipType(), "membershipType");
    }
}
