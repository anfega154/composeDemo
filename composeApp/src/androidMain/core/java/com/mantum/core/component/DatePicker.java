package com.mantum.core.component;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.mantum.core.R;
import com.mantum.core.util.Assert;

import java.util.Calendar;
import java.util.TimeZone;

@Deprecated
public final class DatePicker implements PickerHelper {

    private final EditText editText;

    private DatePicker(EditText editText) {
        this.editText = editText;
    }

    public String value() {
        return !Assert.isNull(this.editText) ? editText.getText().toString() : null;
    }
    public boolean valid() {
        if (!Assert.isNull(this.editText)) {
            this.editText.setError(null);
            return !this.editText.getText().toString().isEmpty();
        }
        return false;
    }

    public static DatePicker use(Context context, @IdRes int id, Calendar calendar) {
        return new DatePicker.Builder(context)
                .id(id)
                .calendar(calendar)
                .callback((Callback) context)
                .cancelable(false)
                .enabled(true)
                .build();
    }

    public void error(String message) {
        this.editText.setError(message);
        this.editText.requestFocus();
    }

    public static class Builder extends PickerAbstract<Builder> {

        private DatePicker.Callback callback;

        public Builder(Context context) {
            super(context);
        }

        public Builder callback(@NonNull DatePicker.Callback callback) {
            this.callback = callback;
            return this;
        }

        @Override
        public <T> T build() {
            DatePickerDialog.OnDateSetListener listener = (view, year, monthOfYear, dayOfMonth) -> Builder.this.callback.dateSet(
                    ((Activity) Builder.this.context).findViewById(Builder.this.id),
                    year, monthOfYear + 1, dayOfMonth
            );

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            final DatePickerDialog datePicker = new DatePickerDialog(
                    this.context, R.style.picker, listener, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.setCancelable(this.cancelable);

            EditText field = (EditText) ((Activity) this.context).findViewById(this.id);
            if (!Assert.isNull(field)) {
                if (!Assert.isNull(this.calendar)) {
                    this.callback.dateSet(field,
                            this.calendar.get(Calendar.YEAR),
                            this.calendar.get(Calendar.MONTH) + 1,
                            this.calendar.get(Calendar.DAY_OF_MONTH));
                }

                field.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (enabled) {
                            datePicker.show();
                        }
                    }
                });
            }
            return (T) new DatePicker(field);
        }
    }

    public interface Callback {

        void dateSet(View view, int year, int month, int day);
    }
}