package com.kc1vmz.netlog.record;

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

import org.springframework.lang.Nullable;
import java.time.LocalDateTime;
import com.kc1vmz.netlog.enums.CommunicationBand;
import com.kc1vmz.netlog.enums.CommunicationPlatform;
import com.kc1vmz.netlog.enums.ElectricalPowerType;


public record ParticipantRecord(
        String id,
        String eventId,
        String operatorId,
        @Nullable LocalDateTime startTime,
        @Nullable LocalDateTime endTime,
        ElectricalPowerType primaryPower,
        ElectricalPowerType backupPower,
        int transmitPower,
        CommunicationPlatform communicationPlatform,
        CommunicationBand communicationBand) {
}
