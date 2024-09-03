package com.mantum.cmms.helper;

import com.mantum.cmms.entity.PendienteMantenimiento;

public class PendienteHelper {

    private final String pendientepmtto;

    public PendienteHelper(String pendientepmtto) {
        this.pendientepmtto = pendientepmtto;
    }

    public String getPendientepmtto() {
        return pendientepmtto;
    }

    public static class PendienteMantenimientoHelper {

        private final PendienteMantenimiento.Request pendientepmtto;

        public PendienteMantenimientoHelper(PendienteMantenimiento.Request pendientepmtto) {
            this.pendientepmtto = pendientepmtto;
        }

        public PendienteMantenimiento.Request getPendientepmtto() {
            return pendientepmtto;
        }
    }
}