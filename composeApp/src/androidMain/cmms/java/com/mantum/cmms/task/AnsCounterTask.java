package com.mantum.cmms.task;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.ANS;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Estado;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.Recorrido;
import com.mantum.cmms.entity.parameter.UserParameter;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.service.ATNotificationService;
import com.mantum.cmms.service.RecorridoService;
import com.mantum.component.Mantum;
import com.mantum.component.service.Notification;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class AnsCounterTask extends Service {

    private static final int PERIOD = 1000 * 60;
    private Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = timer != null ? timer : new Timer();
        timer.scheduleAtFixedRate(new AnsCounterTask.Task(this), 0, PERIOD);
    }

    public static class Task extends TimerTask {

        private final Context context;

        private final Handler handler = new Handler();

        private final int chaneld_id = new Random().nextInt(999);

        public Task(@NonNull Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            if (!Mantum.isConnectedOrConnecting(context)) {
                return;
            }

            if (UserPermission.check(context, UserPermission.MODULO_PANEL_GESTION_SERVICIO, false)) {
                handler.postDelayed(this::process, PERIOD);
            } else {
                handler.removeCallbacks(this::process);
            }
        }

        public void process() {
            RecorridoService recorridoService = new RecorridoService(
                    context, RecorridoService.Tipo.OT);

            Database database = new Database(context);
            Cuenta cuenta = database.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null)
                return;

            Notification notification = new Notification(context, cuenta.getUUID(), chaneld_id);

            Recorrido recorrido = recorridoService.obtenerActual();
            if (recorrido != null && recorrido.getEstado() != null) {
                ATNotificationService.Estado estadoActual = ATNotificationService.Estado.getEstado(context, recorrido.getEstado());
                OrdenTrabajo ot = database.where(OrdenTrabajo.class)
                        .equalTo("id", recorrido.getIdmodulo())
                        .equalTo("cuenta.UUID", cuenta.getUUID())
                        .findFirst();

                if (ot != null && ot.getAns().size() > 0 && estadoActual != null) {
                    Estado estado = database.where(Estado.class)
                            .equalTo("id", estadoActual.getId())
                            .equalTo("cuenta.UUID", cuenta.getUUID())
                            .findFirst();

                    if (estado != null) {
                        for (ANS value : ot.getAns()) {
                            int ejecucionInicial = value.getEjecucioninicial() != null ? value.getEjecucioninicial() : 0;
                            int ejecucionFinal = value.getEjecucionfinal() != null ? value.getEjecucionfinal() : 900;

                            if (value.getFechafin() == null && estado.getEjecucion() >= ejecucionInicial && estado.getEjecucion() <= ejecucionFinal) {

                                String texto = null;
                                String rango = UserParameter.getValue(context, UserParameter.RANGO_TIEMPO_ANS);

                                Calendar now = Calendar.getInstance();
                                long diff = value.getVencimiento().getTime() - now.getTime().getTime();

                                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                                if (minutes > 0) {
                                    if (rango != null && Long.parseLong(rango) == minutes) {
                                        texto = "Faltan: " + minutes + " minutos para que se cumpla el ANS.";
                                    }
                                } else {
                                    minutes = Math.abs(minutes);
                                    if (rango != null && Long.parseLong(rango) == minutes) {
                                        texto = "Estás atrasado: " + minutes + " minutos en el ANS";
                                    }
                                }

                                if (texto != null) {
                                    notification.show(new Notification.Model(
                                            context.getString(R.string.notification_time_ans) + " - " + ot.getCodigo(),
                                            String.format("%s - %s", value.getNombre(), texto)));
                                }

                                return;
                            } else {
                                Notification.cancel(context, chaneld_id);
                            }
                        }
                    }
                }
            } else {
                Notification.cancel(context, chaneld_id);
            }
        }
    }
}