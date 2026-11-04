package com.kc1vmz.netlog.web;

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


import java.net.URI;
import org.springframework.web.util.UriComponentsBuilder;

/** Retains URI path normalization and escaping for report download redirects. */
public final class WebUris {

    private WebUris() {
    }

    public static URI path(String path) {
        return UriComponentsBuilder.fromPath(path).build().encode().toUri();
    }
}
