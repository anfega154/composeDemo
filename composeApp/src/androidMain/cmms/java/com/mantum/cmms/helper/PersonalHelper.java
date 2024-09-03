package com.mantum.cmms.helper;

import com.mantum.cmms.entity.Personal;
import com.mantum.component.service.Photo;

import java.util.ArrayList;
import java.util.List;

public class PersonalHelper {


    private final List<Personal> grupos;

    public PersonalHelper() {
        grupos = new ArrayList<>();
    }

    public List<Personal> getGroup() {
        return grupos;
    }
}
