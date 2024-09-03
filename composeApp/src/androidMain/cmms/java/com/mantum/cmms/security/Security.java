package com.mantum.cmms.security;

import com.mantum.cmms.entity.Accion;

import io.realm.RealmList;

public class Security {

    public static final String TAG = Security.class.getSimpleName();

    public static final String ACTION_REGISTRAR_SS = "SS";

    public static final String ACTION_REGISTRAR_BITACORA = "Bit";

    public static boolean action(RealmList<Accion> actions, String key) {
        try {
            if (actions == null || actions.isEmpty()) {
                return false;
            }

            boolean response = false;
            for (Accion action : actions) {
                if (key.equals(action.getName())) {
                    response = true;
                    break;
                }
            }

            return response;
        } catch (Exception e) {
            return true;
        }
    }
}