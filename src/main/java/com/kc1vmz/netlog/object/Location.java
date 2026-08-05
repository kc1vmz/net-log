package com.kc1vmz.netlog.object;

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

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.annotation.Serdeable.Deserializable;

@Serdeable
@Deserializable
public class Location {
    private String id;
    private String name;
    private LocationCountry country;
    private LocationState state;
    private LocationCounty county;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public LocationCountry getCountry() {
        return country;
    }
    public void setCountry(LocationCountry country) {
        this.country = country;
    }
    public LocationState getState() {
        return state;
    }
    public void setState(LocationState state) {
        this.state = state;
    }
    public LocationCounty getCounty() {
        return county;
    }
    public void setCounty(LocationCounty county) {
        this.county = county;
    }
}
