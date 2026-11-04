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


import java.beans.PropertyEditorSupport;
import com.kc1vmz.netlog.controller.UIController;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/** Micronaut supplied zero for omitted primitive components in form request records. */
@ControllerAdvice(assignableTypes = UIController.class)
public class FormBindingAdvice {

    @InitBinder
    public void bindOptionalIntegers(WebDataBinder binder) {
        binder.registerCustomEditor(int.class, new PropertyEditorSupport() {
            @Override
            public void setValue(Object value) {
                super.setValue(value == null ? 0 : value);
            }

            @Override
            public void setAsText(String text) {
                setValue(text == null || text.isEmpty() ? 0 : Integer.valueOf(text));
            }
        });
    }
}
