package com.kc1vmz.netlog.accessor;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kc1vmz.netlog.object.Section;
import com.kc1vmz.netlog.record.SectionRecord;
import com.kc1vmz.netlog.repository.SectionRepository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SectionAccessor {
    @Inject
    private SectionRepository sectionRepository;
    private static final Logger logger = LogManager.getLogger(SectionAccessor.class);

    public List<Section> list() {
        return list(true);
    }

    public List<Section> list(boolean activeOnly) {
        List<Section> ret = new ArrayList<>();

        try {
            List<SectionRecord> records =  sectionRepository.findAll();
            if (records != null) {
                for (SectionRecord record : records) {
                    if (activeOnly) {
                        if (!record.active()) {
                            continue;
                        }
                    }
                    Section section = new Section();
                    section.setId(record.id());
                    section.setName(record.name());
                    section.setDescription(record.description());
                    section.setActive(record.active());
                    ret.add(section);
                }
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }

        Collections.sort(ret, new Comparator<Section>() {
            @Override
            public int compare(Section obj1, Section obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });

        return ret;
    }

    public Section create(Section section) {
        try {
            SectionRecord rec = new SectionRecord(UUID.randomUUID().toString(), section.getName(), section.getDescription(), true);
            SectionRecord recNew = sectionRepository.save(rec);
            section.setId(recNew.id());
            return get(recNew.id());
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }

    public Section get(String id) {
        Section ret = null;

        try {
            Optional<SectionRecord> recordOpt =  sectionRepository.findById(id);
            if (recordOpt.isPresent()) {
                SectionRecord record = recordOpt.get();
                ret = new Section();
                ret.setId(record.id());
                ret.setName(record.name());
                ret.setDescription(record.description());
                ret.setActive(record.active());
            }
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return ret;
    }

    public void delete(String id, boolean soft) {
        if (soft) {
            try {
                Section section = get(id);
                section.setActive(false);
                update(id, section);
            } catch (Exception e) {
            }
        } else {
            sectionRepository.deleteById(id);
        }
    }

    public Section update(String id, Section obj) {
        try {
            Optional<SectionRecord> recordOpt =  sectionRepository.findById(id);
            if (!recordOpt.isPresent()) {
                return null;
            }

            SectionRecord rec = recordOpt.get();
            SectionRecord recUpdate = new SectionRecord(rec.id(), obj.getName(), obj.getDescription(), obj.isActive());
            sectionRepository.update(recUpdate);
            obj.setId(id);
            return obj;
        } catch (Exception e) {
            logger.error("Exception caught", e);
        }
        return null;
    }
}
