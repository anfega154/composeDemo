package com.mantum.core.component;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.mantum.demo.R;import com.mantum.core.util.Assert;

import java.util.Calendar;
import java.util.TimeZone;

@Deprecated
public final class TimePicker implements PickerHelper {

    private final EditText editText;

    private TimePicker(EditText editText) {
        this.editText = editText;
    }

    public boolean valid() {
        if (!Assert.isNull(this.editText)) {
            this.editText.setError(null);
            return !this.editText.getText().toString().isEmpty();
        }
        return false;
    }

    public void error(String message) {
        this.editText.setError(message);
        this.editText.requestFocus();
    }

    public String value() {
        return !Assert.isNull(this.editText) ? editText.getText().toString() : "";
    }

    /**
     * Construye el componente de hora
     *
     * @param context {@link Context}
     * @param id Identificador del componente
     * @param calendar {@link Calendar}
     * @return {@link TimePicker}
     */
    public static TimePicker use(Context context, @IdRes int id, Calendar calendar) {
        return new Builder(context)
                .id(id)
                .calendar(calendar)
                .callback((Callback) context)
                .cancelable(false)
                .enabled(true)
                .build();
    }

    /**
     * Construye el componente de hora
     *
     * @param context {@link Context}
     * @param id Identificador del componente
     * @return {@link TimePicker}
     */
    public static TimePicker use(Context context, @IdRes int id) {
        return TimePicker.use(context, id, null);
    }

    /**
     * Contiene el método que es utilizado al seleccionar la hora
     * del componente
     *
     * @author Jonattan Velásquez
     */
    public interface Callback {

        /**
         * Hora y minuto seleccionado en el componente
         * @param view {@link View}
         * @param hourOfDay hora
         * @param minute minutos
         */
        void timeSet(View view, int hourOfDay, int minute);
    }

    /**
     * Contiene los métodos necesarios para construir el componente de hora
     * @author Jonattan Velásquez
     */
    public static class Builder extends PickerAbstract<Builder> {

        private TimePicker.Callback callback;

        /**
         * Obtiene una nueva instancia del objeto
         * @param context {@link Context}
         */
        public Builder(Context context) {
            super(context);
        }

        @Override
        public <T> T build() {
            final Builder self = this;
            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
                    if (!Assert.isNull(self.callback)) {
                        self.callback.timeSet(((Activity) Builder.this.context).findViewById(Builder.this.id), hourOfDay, minute);
                    }
                }
            };

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            final TimePickerDialog timePicker = new TimePickerDialog(
                    this.context,
                    R.style.picker,
                    listener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), true
            );
            timePicker.setCancelable(this.cancelable);

            EditText field = (EditText) ((Activity) this.context).findViewById(this.id);
            if (!Assert.isNull(field)) {
                if (!Assert.isNull(this.calendar)) {
                    this.callback.timeSet(field,
                            this.calendar.get(Calendar.HOUR_OF_DAY), this.calendar.get(Calendar.MINUTE));
                }

                field.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (enabled) {
                            timePicker.show();
                        }
                    }
                });
            }
            return (T) new TimePicker(field);
        }

        /**
         * Agrega el callback al seleccionar la hora del componente
         *
         * @param callback {@link TimePicker.Callback}
         * @return {@link Picker}
         */
        public Builder callback(@NonNull TimePicker.Callback callback) {
            this.callback = callback;
            return this;
        }
    }
}