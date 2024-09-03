package com.mantum.cmms.service;

import androidx.annotation.Nullable;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonObject;

public class SocketService {

    private static final String TAG = SocketService.class.getSimpleName();

    private static final SocketService INSTANCE = new SocketService();

    public Socket mSocket;

    private boolean isLive = false;

    private Long cuenta;

    public static final String SOCKET_URL = "sUrlWebsocket";

    private double latitude;
    private double longitude;

    public static final String ON = "isConnected";
    public static final String OFF = "disconnect";
    public static final String RECONNECT = "reconnect_attempt";
    public static final String SEARCH_LOCATION = "searchingLocation";
    public static final String CHANGE_STATUS = "changeStatus";
    public static final String SEND_LOCATION = "sendLocation";
    public static final String LOGIN = "login";

    public boolean isLive() {
        return isLive;
    }

    private void setLive(boolean live) {
        isLive = live;
    }

    public void setCuenta(Long cuenta) {
        this.cuenta = cuenta;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    private SocketService() {
    }

    public void initSocket(String socketUrl) {
        try {
            mSocket = IO.socket(socketUrl);
        } catch (Exception ignored) {}
    }

    public static SocketService getInstance() {
        return INSTANCE;
    }

    public void loginSocket() {
        try {
            JsonObject login = new JsonObject();
            login.addProperty("id", cuenta);
            login.addProperty("token", "94nt1@41s1n9@t@!3112t4smns@@s!ms");
            mSocket.emit(LOGIN, login);
        } catch (Exception e) {
            Log.e(TAG, "loginSocket: ", e);
        }
    }

    private Emitter.Listener searchingLocation = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            try {
                JsonObject location = new JsonObject();
                location.addProperty("latitude", latitude);
                location.addProperty("longitude", longitude);
                mSocket.emit(SEND_LOCATION, location);
            } catch (Exception e) {
                Log.e(TAG, "call: ", e);
            }
        }
    };

    void setChangeStatusSocket(int estado, @Nullable String obs) {
        try {
            JsonObject state = new JsonObject();
            state.addProperty("idestado", estado);
            state.addProperty("observacion", obs);
            mSocket.emit(CHANGE_STATUS, state);
        } catch (Exception e) {
            Log.e(TAG, "setChangeStatusSocket: ", e);
        }
    }

    public Emitter.Listener on = args -> {
        try {
            setLive(true);
            mSocket.on(SEARCH_LOCATION, searchingLocation);
        } catch (Exception e) {
            Log.e(TAG, "on: ", e);
        }
    };

    public Emitter.Listener off = args -> setLive(false);

    public Emitter.Listener reconnected = args -> {
      try {
          loginSocket();
      }  catch (Exception e) {
          Log.e(TAG, "reconnected: ", e);
      }
    };
}