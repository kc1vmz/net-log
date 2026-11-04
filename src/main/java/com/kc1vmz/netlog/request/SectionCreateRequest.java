package com.kc1vmz.netlog.request;

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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectionCreateRequest(
        @NotBlank String name,
        String description,
        String callsignMemberIds,
        @Size(max = 100) String leaderName,
        @Size(max = 100) String leaderContact) {

    public SectionCreateRequest(String name, String description, String callsignMemberIds) {
        this(name, description, callsignMemberIds, null, null);
    }
}
