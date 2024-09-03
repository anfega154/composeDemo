package com.mantum.cmms.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;

public abstract class Version {

    // cd7e68d243f7c831cb771281e8e685b4
    public static String build(@NonNull String version) {
        return "application/vnd.mantum.app-v" + version + "+json";
    }

    /**
     * @deprecated Agregar el número de la versión en la cuenta de cada usuario
     */
    @Deprecated
    public static boolean check(@NonNull Context context, @NonNull Integer number) {
        Database database = new Database(context);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return false;
        }

        com.mantum.cmms.entity.Version version = database.where(com.mantum.cmms.entity.Version.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findFirst();

        boolean response = version != null && version.getVersion() != null && version.getVersion() >= number;
        database.close();

        return response;
    }

    public static void save(@NonNull Context context, @Nullable String number) {
        save(context, number != null && !number.isEmpty() ? Integer.parseInt(number) : null);
    }

    public static void save(@NonNull Context context, @Nullable Integer number) {
        if (number == null) {
            return;
        }

        Database database = new Database(context);
        database.executeTransaction(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                return;
            }

            com.mantum.cmms.entity.Version version = self.where(com.mantum.cmms.entity.Version.class)
                    .equalTo("cuenta.UUID", cuenta.getUUID()).findFirst();

            if (version == null) {
                com.mantum.cmms.entity.Version newVersion = new com.mantum.cmms.entity.Version();
                newVersion.setCuenta(cuenta);
                newVersion.setVersion(number);
                self.insert(newVersion);
            } else if (version.getVersion() != null) {
                version.setVersion(number);
            }
        });

        database.close();
    }

    @Nullable
    @Deprecated
    public static Integer get(@NonNull Context context) {
        Database database = new Database(context);
        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return null;
        }

        com.mantum.cmms.entity.Version version = database.where(com.mantum.cmms.entity.Version.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findFirst();

        if (version == null) {
            return null;
        }

        int response = version.getVersion();
        database.close();
        return response;
    }
}