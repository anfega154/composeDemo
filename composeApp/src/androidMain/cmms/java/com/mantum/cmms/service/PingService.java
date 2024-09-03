package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.mantum.demo.R;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.net.ClientManager;
import com.mantum.cmms.util.Preferences;
import com.mantum.cmms.util.Version;
import com.mantum.component.http.MicroServices;
import com.mantum.component.util.Tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.mantum.component.Mantum.isConnectedOrConnecting;

public class PingService extends MicroServices {

    private static final String TAG = PingService.class.getSimpleName();

    private final String version;

    public PingService(@NonNull Context context, @NonNull Cuenta cuenta) {
        super(context, cuenta.getServidor().getUrl(), cuenta.getToken(context), ClientManager.prepare(
                new OkHttpClient.Builder(), context
        ));
        version = cuenta.getServidor().getVersion();
    }

    public Observable<ResponseBodyGet> getPingTest(boolean sendFile) {
        return Observable.create(subscriber -> {
            if (!isConnectedOrConnecting(context)) {
                subscriber.onError(new Exception(context.getString(R.string.offline)));
                return;
            }

            Request request;
            String url = Preferences.url(context, "restapp/app/pingtest");
            if (sendFile) {
                MultipartBody.Builder multipart = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", Tool.formData(createFile()));

                RequestBody body = multipart.build();

                request = new Request.Builder().url(url)
                        .addHeader("token", Preferences.token(context))
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(version))
                        .post(body).build();
            } else {
                request = new Request.Builder().get().url(url)
                        .addHeader("token", Preferences.token(context))
                        .addHeader("cache-control", "no-cache")
                        .addHeader("accept-language", "application/json")
                        .addHeader("accept", Version.build(version))
                        .build();
            }

            Log.e(TAG, "build: " + request);
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (!call.isCanceled()) {
                        subscriber.onError(e);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response)
                        throws IOException {
                    if (call.isCanceled()) {
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        subscriber.onError(new Exception(context.getString(R.string.request_error_search)));
                        return;
                    }

                    String json = body.string();
                    try {
                        if (response.isSuccessful()) {
                            ResponseBodyGet content = new Gson().fromJson(json, ResponseBodyGet.class);

                            subscriber.onNext(content);
                            subscriber.onComplete();
                        } else {
                            subscriber.onError(new Exception(context.getString(R.string.error_app)));
                        }
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                    response.close();
                }
            });
        });
    }

    public static class ResponseBodyGet {
        Body body;

        public Body getBody() {
            return body;
        }
    }

    public static class Body {
        String message;

        public String getMessage() {
            return message;
        }
    }

    private File createFile() {
        try {
            File file = File.createTempFile("file", ".txt", context.getExternalCacheDir());

            String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean malesuada lectus erat, vitae interdum sapien pharetra ac. Etiam ultrices, ligula sit amet ultricies interdum, urna augue malesuada risus, ut consequat ante orci sit amet quam. Cras aliquam mi sed lacus vestibulum, at vulputate quam blandit. Praesent iaculis tempor convallis. Fusce luctus facilisis justo, in cursus odio scelerisque sed. Nunc ac imperdiet diam. Donec sapien lorem, sollicitudin eget magna id, sollicitudin lobortis leo. Phasellus ac massa lacus. Quisque iaculis massa odio, ut maximus urna lobortis a.\n\n" +
                    "Vestibulum hendrerit lectus at diam tristique sollicitudin. Vestibulum imperdiet nibh eget ultricies ultrices. Quisque ac magna dui. Sed sed ipsum ipsum. Vivamus consectetur mauris eget risus cursus suscipit. Donec semper, augue ut commodo laoreet, tortor augue eleifend nisl, vel pretium enim nunc at leo. Ut faucibus lacus ut mauris volutpat facilisis. Mauris non justo felis. Donec vel felis nisi.\n\n" +
                    "Curabitur eu lacus tortor. Nullam hendrerit sem nec nisi pretium, eu blandit lectus suscipit. Nunc neque nulla, lacinia in nulla vitae, tincidunt ornare lectus. Nullam porttitor turpis vel leo tincidunt, et rutrum risus venenatis. Interdum et malesuada fames ac ante ipsum primis in faucibus. Etiam id rutrum nunc. Vestibulum sit amet fermentum neque, laoreet hendrerit neque. Morbi eget bibendum risus.\n\n" +
                    "Nam faucibus et leo at congue. Nullam in semper odio, non tincidunt risus. Mauris ullamcorper eros vitae tempus dictum. Quisque ornare justo at semper convallis. Pellentesque consectetur consectetur risus sed tempor. Sed vulputate odio nec lectus hendrerit finibus. In ornare, metus et vestibulum tincidunt, urna quam auctor libero, vel lacinia ante arcu ut ante. Etiam congue ornare erat cursus tincidunt. Aliquam pharetra libero non leo ultrices, vitae finibus purus ultrices. Curabitur commodo tristique sapien. Nunc dignissim arcu sed commodo suscipit. Morbi cursus, justo id mollis tempor, ligula leo sodales risus, et mattis erat est sit amet ligula.\n\n" +
                    "Sed vehicula euismod blandit. Etiam sed accumsan leo, vel consectetur elit. Nulla sit amet purus metus. Ut semper quis erat vel tincidunt. Fusce aliquet condimentum leo eu ornare. Proin tristique libero sit amet risus pharetra, mollis maximus nulla convallis. Vestibulum ut urna sed tortor vulputate congue sit amet non mauris. Pellentesque cursus mollis nisi. Duis consequat metus ut scelerisque suscipit.";

            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            while (file.length() < 1000000) {
                bufferedWriter.write(lorem);
            }
            bufferedWriter.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
