package com.mantum.core.component;

import android.content.Context;
import androidx.annotation.IdRes;

import java.util.Calendar;

@Deprecated
public abstract class PickerAbstract<T extends PickerHelper> implements Picker<T> {

    protected int id;

    protected final Context context;

    protected boolean cancelable;

    protected Calendar calendar;

    protected boolean enabled;

    /**
     * Obtiene una nueva instancia del objeto
     * @param context {@link Context}
     */
    public PickerAbstract(Context context) {
        this.context = context;
        this.enabled = true;
        this.cancelable = false;
    }

    public T id(@IdRes int id) {
        this.id = id;
        return (T) this;
    }

    public T cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return (T) this;
    }

    public T calendar(Calendar calendar) {
        this.calendar = calendar;
        return (T) this;
    }

    public T enabled(boolean enabled) {
        this.enabled = enabled;
        return (T) this;
    }

    /**
     * Contruye el componente según la configuración dada
     *
     * @param <T> {@link PickerHelper}
     * @return {PickerHelper}
     */
    public abstract <T> T build();

    public static String normalize(int number) {
        return String.valueOf(number <= 9 ? "0" + number : number);
    }
}