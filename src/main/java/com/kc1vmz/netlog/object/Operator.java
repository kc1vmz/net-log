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
public class Operator {
    private String id;
    private String callsign;
    private String name;
    private boolean isNTS;
    private boolean isSkywarn;
    private boolean isRACES;
    private Location location;

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
    public String getCallsign() {
        return callsign;
    }
    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }
    public boolean isNTS() {
        return isNTS;
    }
    public void setNTS(boolean isNTS) {
        this.isNTS = isNTS;
    }
    public boolean isSkywarn() {
        return isSkywarn;
    }
    public void setSkywarn(boolean isSkywarn) {
        this.isSkywarn = isSkywarn;
    }
    public boolean isRACES() {
        return isRACES;
    }
    public void setRACES(boolean isRACES) {
        this.isRACES = isRACES;
    }
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
}
