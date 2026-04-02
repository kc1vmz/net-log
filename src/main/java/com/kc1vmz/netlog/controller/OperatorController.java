package com.kc1vmz.netlog.controller;

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

import com.kc1vmz.netlog.accessor.OperatorAccessor;
import com.kc1vmz.netlog.object.Operator;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

@Controller("/api/v1/operators")
public class OperatorController {

    @Inject
    private OperatorAccessor operatorAccessor;

    @Get
    public List<Operator> list() {
        return operatorAccessor.list();
    }

    @Get("/{id}")
    public Operator get(@PathVariable String id) {
        return operatorAccessor.get(id);
    }

    @Put("/{id}")
    public Operator update(@PathVariable String id, @Body Operator obj) {
        return operatorAccessor.update(id, obj);
    }

    @Delete("/{id}")
    public void delete(@PathVariable String id) {
        operatorAccessor.delete(id);
    }

    @Post
    public HttpResponse<Operator> create(@Body Operator operator) {
        Operator operatorNew = operatorAccessor.create(operator);
        if (operatorNew == null) {
            return HttpResponse.badRequest();
        }

        return HttpResponse.created(operatorNew);
    }
}