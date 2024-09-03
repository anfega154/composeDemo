package com.mantum.cmms.service;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mantum.R;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Transaccion;
import com.mantum.cmms.entity.parameter.UserPermission;
import com.mantum.cmms.task.TransaccionTask;
import com.mantum.component.Mantum;
import com.mantum.component.service.Notification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class TransaccionService {

    private final Context context;

    private final Database database;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AlertDialog alertDialog;

    private String message;

    private boolean sesionTransaction = false;

    public void setSesionTransaction(boolean sesionTransaction) {
        this.sesionTransaction = sesionTransaction;
    }

    public TransaccionService(@NonNull Context context) {
        this.context = context;
        this.database = new Database(context);
    }

    public Observable<List<Transaccion>> save(@NonNull final Transaccion transaccion) {
        return save(Collections.singletonList(transaccion));
    }

    public Observable<List<Transaccion>> save(@NonNull List<Transaccion> transaccions) {
        boolean directTransaction = UserPermission.check(context, UserPermission.REALIZAR_TRANSACCIONES_DIRECTAS, false);
        boolean directTransactionMovements = UserPermission.check(context, UserPermission.TRANSACCIONES_DIRECTAS_MOVIMIENTOS, false);

        String[] arrayModulos = new String[]{
                Transaccion.MODULO_BITACORA,
                Transaccion.MODULO_RUTA_TRABAJO,
                Transaccion.MODULO_ALMACEN,
                Transaccion.MODULO_ORDEN_TRABAJO,
                Transaccion.MODULO_ESTADO_USUARIO,
                Transaccion.MODULO_EQUIPOS,
                Transaccion.MODULO_FALLAS,
                Transaccion.MODULO_CORREO,
                Transaccion.MODULO_MARCACION,
                Transaccion.MODULO_LISTA_CHEQUEO,
                Transaccion.MODULO_OT_LISTA_CHEQUEO
        };
        List<String> directModulos = Arrays.asList(arrayModulos);

        Transaccion currentTransaction = transaccions.get(0);
        String currentModulo = currentTransaction.getModulo();

        Cuenta cuenta = database.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if ((directTransactionMovements && Transaccion.MODULO_MOVIMIENTO.equals(currentModulo)) || (directTransaction && directModulos.contains(currentModulo)) || sesionTransaction) {

            Transaccion element = currentTransaction.isManaged()
                    ? database.copyFromRealm(currentTransaction)
                    : currentTransaction;

            Notification notification = new Notification(context, element.getUUID());

            TransaccionTask.Task functions = new TransaccionTask.Task(this.context);

            return Observable.create(emitter -> compositeDisposable
                    .add(functions.onPush(element)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                        functions.onNext(element.getUUID(), response, element);
                                        emitter.onNext(transaccions);

                                        if (currentModulo.equals(Transaccion.MODULO_FALLAS)) {
                                            element.setRespuesta(new Gson().toJson(response));
                                        }
                                    },
                                    throwable -> {
                                        database.insert(element);
                                        String errorMessage = throwable.getMessage();
                                        if (errorMessage != null) {
                                            if (errorMessage.startsWith("Failed to connect to")) {
                                                errorMessage = "Falló la conexión al servidor: " + throwable.getMessage();
                                            } else if (errorMessage.startsWith("Attempt to invoke interface method")) {
                                                errorMessage = "Ocurrió un problema al tratar de obtener las imágenes, por favor cambie la ruta e intente de nuevo. \n\nError técnico: " + throwable.getMessage();
                                            }
                                        }

                                        alertDialog = new AlertDialog.Builder(context)
                                                .setTitle(R.string.transaccion_direct_title)
                                                .setMessage(errorMessage)
                                                .setPositiveButton(R.string.error_detail, null)
                                                .setNegativeButton(R.string.close, (dialogInterface, id) -> alertDialog.dismiss())
                                                .setCancelable(false)
                                                .create();

                                        message = throwable.getMessage();
                                        alertDialog.setOnShowListener(dialog -> {
                                            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                            boolean responseException = throwable instanceof Mantum.ResponseException;
                                            if (responseException) {
                                                Mantum.ResponseException exception = (Mantum.ResponseException) throwable;
                                                message = Mantum.toPrettyFormat(exception.getBody());
                                            }
                                            element.setRespuesta(message);
                                            button.setOnClickListener(view -> {
                                                TextView text = alertDialog.findViewById(android.R.id.message);
                                                text.setBackgroundColor(ContextCompat.getColor(text.getContext(), R.color.gray));
                                                alertDialog.setMessage(message);
                                                button.setText(R.string.share_detail);

                                                button.setOnClickListener(view1 -> {
                                                    if (cuenta != null) {
                                                        SendEmailService.shareTransactionDetail(context, cuenta, element);
                                                    }
                                                });
                                            });
                                        });
                                        alertDialog.show();
                                        emitter.onError(throwable);
                                    }, () -> {
                                        functions.onComplete(notification, element);
                                        emitter.onComplete();
                                    }
                            )));
        } else {
            return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
                for (Transaccion transaccion : transaccions) {
                    if (Transaccion.MODULO_GEOLOCALIZACION.equals(transaccion.getModulo())) {
                        transaccion.setPrioridad(0);
                    }
                    self.insertOrUpdate(transaccion);
                }
                subscriber.onNext(transaccions);
            }, subscriber::onComplete, subscriber::onError));
        }
    }

    public Observable<List<Transaccion>> save(@Nullable String uuid, @NonNull Transaccion value) {
        return Observable.create(subscriber -> database.executeTransactionAsync(self -> {
            Cuenta cuenta = self.where(Cuenta.class)
                    .equalTo("active", true)
                    .findFirst();

            if (cuenta == null) {
                subscriber.onError(new Exception(context.getString(R.string.error_authentication)));
                return;
            }

            if (Transaccion.MODULO_GEOLOCALIZACION.equals(value.getModulo())) {
                value.setPrioridad(0);
            }

            Transaccion transaccion = self.where(Transaccion.class)
                    .equalTo("UUID", uuid)
                    .equalTo("cuenta.UUID", cuenta.getUUID())
                    .findFirst();

            if (transaccion == null) {
                self.insert(value);
                subscriber.onNext(Collections.singletonList(value));
                return;
            }

            transaccion.setCreation(value.getCreation());
            transaccion.setValue(value.getValue());
            transaccion.setMessage("");
            transaccion.setUrl(value.getUrl());
            transaccion.setEstado(value.getEstado());

            subscriber.onNext(Collections.singletonList(transaccion));
        }, subscriber::onComplete, subscriber::onError));
    }

    public void close() {
        database.close();
    }
}