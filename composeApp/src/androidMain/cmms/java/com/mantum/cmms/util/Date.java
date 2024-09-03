package com.mantum.cmms.util;

import com.mantum.core.component.PickerAbstract;

import java.util.Calendar;
import java.util.TimeZone;

@Deprecated
public class Date {

    public static String date() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        return String.format("%s-%s-%s", calendar.get(Calendar.YEAR),
                PickerAbstract.normalize(calendar.get(Calendar.MONTH) + 1),
                PickerAbstract.normalize(calendar.get(Calendar.DAY_OF_MONTH)));
    }

    public static String now() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String date = date();
        return String.format("%s %s:%s:%s.%s", date,
                PickerAbstract.normalize(calendar.get(Calendar.HOUR_OF_DAY)),
                PickerAbstract.normalize(calendar.get(Calendar.MINUTE)),
                PickerAbstract.normalize(calendar.get(Calendar.SECOND)),
                calendar.get(Calendar.MILLISECOND));
    }
}