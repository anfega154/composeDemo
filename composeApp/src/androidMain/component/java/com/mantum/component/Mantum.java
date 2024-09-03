package com.mantum.component;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.mantum.component.adapter.handler.OnCompare;
import com.mantum.component.adapter.handler.ViewAdapterHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE;

public abstract class Mantum {

    public static final String PRIMARY_COLOR = "#FF5600";

    public static final String KEY_ID = "key_id";

    public static final String ENTITY_TYPE = "entity_type";

    public static final String KEY_UUID = "KEY_UUID";

    public static final String KEY_REFRESH = "key_refresh";

    public static final String KEY_MESSAGE = "message";

    public static void ignoreError(Throwable error) {
        Log.e("Ignore", "Error: ", error);
    }

    public static Gson getGson() {
        // Trick to get the DefaultDateTypeAdatpter instance
        // Create a first instance a Gson
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm")
                .create();

        // Get the date adapter
        TypeAdapter<Date> dateTypeAdapter = gson.getAdapter(Date.class);

        // Ensure the DateTypeAdapter is null safe
        TypeAdapter<Date> safeDateTypeAdapter = dateTypeAdapter.nullSafe();

        // Build the definitive safe Gson instance
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm")
                .registerTypeAdapter(Date.class, safeDateTypeAdapter)
                .create();
    }

    public static <Type> List<Type> asList(SparseArray<Type> sparseArray) {
        if (sparseArray == null) {
            return new ArrayList<>();
        }

        List<Type> arrayList = new ArrayList<>();
        for (int i = 0; i < sparseArray.size(); i++) {
            arrayList.add(sparseArray.valueAt(i));
        }
        return arrayList;
    }

    @NonNull
    public static String md5(@NonNull String string) {
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(string.getBytes());
            byte[] messageDigest = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2) {
                    h.insert(0, "0");
                }
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ignored) {
        }
        return "";
    }

    @Nullable
    public static Bundle bundle(@NonNull Context context) {
        try {
            ApplicationInfo application = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            return application.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isConnectedOrConnecting(@NonNull Context context) {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager == null) {
                return false;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo == null) {
                    return false;
                }

                return networkInfo.isConnectedOrConnecting();
            } else {
                Network network = connectivityManager.getActiveNetwork();
                if (network == null) {
                    return false;
                }

                final NetworkCapabilities networkCapabilities
                        = connectivityManager.getNetworkCapabilities(network);

                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTablet(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @SuppressWarnings("unused")
    @Nullable
    public static Integer versionCode(@NonNull Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static String versionName(@NonNull Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static void goPlayStore(@NonNull Context context) {
        goPlayStore(context, context.getPackageName());
    }

    public static void goPlayStore(@NonNull Context context, @NonNull String packageName) {
        try {
            context.startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + packageName)));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    public static void call(@NonNull Context context, @NonNull String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    public static void goGoogleMap(@NonNull Context context, @NonNull String url) {
        try {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public static Bitmap convertToBase64(@NonNull String base) {
        try {
            if (base.isEmpty()) {
                throw new Exception("The image is empty");
            }

            String[] image = base.split(",");
            String normalize = image[image.length == 2 ? 1 : 0];
            byte[] data = Base64.decode(normalize, 0);
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isDiskSpace() {
        try {
            File path = Environment.getDataDirectory();
            StatFs statFs = new StatFs(path.getPath());

            long bytesAvailable = SDK_INT >= JELLY_BEAN_MR2
                    ? statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong()
                    : statFs.getBlockSize() * (long) statFs.getAvailableBlocks();

            float size = bytesAvailable / (1024.f * 1024.f);
            return size > 200;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isWhitelist(@NonNull Context context) {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = context.getPackageName();
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(packageName);
        }
        return true;
    }

    public static void whitelist(@NonNull Context context) {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = context.getPackageName();
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setTitle(R.string.ingorar_optimizacion_bateria);
                alertDialogBuilder.setMessage(R.string.ingorar_optimizacion_bateria_mensaje);
                alertDialogBuilder.setPositiveButton(R.string.si, (dialog, which) -> {
                    dialog.cancel();

                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    context.startActivity(intent);
                });

                alertDialogBuilder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
                alertDialogBuilder.show();
            }
        }
    }

    public static boolean deepLink(@NonNull Context context, @NonNull String uriString) {
        return deepLink(context, Uri.parse(uriString));
    }

    public static boolean deepLink(@NonNull Context context, @NonNull Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void goLink(@NonNull Context context, @NonNull String uriString) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        context.startActivity(browserIntent);
    }

    @NonNull
    @Deprecated
    public static RecyclerView initRecyclerView(@IdRes int id, @NonNull View view,
                                                @NonNull RecyclerView.LayoutManager linearLayoutManager,
                                                @NonNull Mantum.Adapter adapter) {
        RecyclerView recyclerView = view.findViewById(id);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        recyclerView.setItemAnimator(itemAnimator);

        return recyclerView;
    }

    public abstract static class OnScrollListener extends RecyclerView.OnScrollListener {

        private boolean loading = true;

        int firstVisibleItem, visibleItemCount, totalItemCount;

        private int current = 1;

        private final LinearLayoutManager linearLayoutManager;

        private final boolean topScroll;

        protected OnScrollListener(@NonNull LinearLayoutManager linearLayoutManager) {
            this(linearLayoutManager, true);
        }

        protected OnScrollListener(@NonNull LinearLayoutManager linearLayoutManager, boolean topScroll) {
            this.linearLayoutManager = linearLayoutManager;
            this.topScroll = topScroll;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (recyclerView.getScrollState() == SCROLL_STATE_IDLE) {
                return;
            }

            if (topScroll) {
                if (!loading && linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    onRequest(1);
                    return;
                }
            }

            if (!loading && dy > 0) {
                totalItemCount = linearLayoutManager.getItemCount();
                firstVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if ((firstVisibleItem + 1) >= totalItemCount) {
                    increment();
                    loading = true;
                    onRequest(current);
                }
            }
        }

        void increment() {
            current = current + 1;
        }

        public void decrease() {
            if (current > 1) {
                current = current - 1;
            }
        }

        public void loading(boolean loading) {
            this.loading = loading;
        }

        public boolean isLoading() {
            return loading;
        }

        public int getPage() {
            return current;
        }

        public abstract void onRequest(int page);
    }

    public abstract static class Activity extends AppCompatActivity {

        public View getView() {
            return findViewById(android.R.id.content);
        }

        public void backActivity() {
            setResult(RESULT_OK);
            finish();
        }

        public void backActivity(String message) {
            Intent intent = new Intent();
            intent.putExtra(KEY_MESSAGE, message);
            setResult(RESULT_OK, intent);
            finish();
        }

        public void backActivity(@NonNull Intent intent) {
            setResult(RESULT_OK, intent);
            finish();
        }

        public void closeKeyboard() {
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }

        public void includeBackButtonAndTitle(@StringRes int string) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                setTitle(getString(string));
            }
        }

        public void includeBackButtonAndTitle(String title) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                setTitle(title);
            }
        }

        public void includeBackButton() {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (getView() != null && bundle != null) {
                    String message = bundle.getString("message");
                    if (message != null && !message.isEmpty()) {
                        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }
    }

    public static class HttpException extends Throwable {

        public HttpException(String message) {
            super(message);
        }
    }

    public static class ResponseException extends Throwable {

        private final String body;

        public ResponseException(String message, String body) {
            super(message);
            this.body = body;
        }

        public String getBody() {
            return body;
        }
    }

    public abstract static class Fragment extends androidx.fragment.app.Fragment {

        @NonNull
        public abstract String getKey();

        @NonNull
        public abstract String getTitle(@NonNull Context context);
    }

    public abstract static class Adapter<K extends ViewAdapterHandler<K>, T extends RecyclerView.ViewHolder>
            extends RecyclerView.Adapter<T> {

        private String color;

        private final List<K> original;

        public List<Integer> selectedIds = new ArrayList<>();

        protected final Context context;

        protected SparseBooleanArray menuVisibility;

        public Adapter(@NonNull Context context) {
            this.context = context;
            this.original = new ArrayList<>();
            this.menuVisibility = new SparseBooleanArray();
            this.color = Mantum.PRIMARY_COLOR;
        }

        public void setMenuVisibility(int id, boolean check) {
            this.menuVisibility.put(id, check);
        }

        @Nullable
        protected K getItemPosition(int position) {
            if (position < 0 || position >= getItemCount()) {
                return null;
            }
            return original.get(position);
        }

        @Override
        public int getItemCount() {
            return original.size();
        }

        public void setColor(@NonNull String color) {
            this.color = color;
        }

        @NonNull
        public String getColor() {
            return color;
        }

        public void add(@NonNull K value) {
            add(value, false);
        }

        public void set(int index, @NonNull K value) {
            original.set(index, value);
        }

        public void add(@NonNull K value, boolean notify) {
            boolean include = true;
            int total = getItemCount();
            for (int i = 0; i < total; i++) {
                if (original.get(i).compareTo(value)) {
                    include = false;
                    original.set(i, value);
                    i = total;
                }
            }

            if (include) {
                original.add(value);
            }

            if (notify) {
                notifyDataSetChanged();
            }
        }

        public void addAll(@NonNull SparseArray<K> values) {
            int total = values.size();
            for (int i = 0; i < total; i++) {
                K value = values.get(i);
                add(value);
            }
        }

        public void addAll(@NonNull List<K> values) {
            addAll(values, true);
        }

        public void addAll(@NonNull List<K> values, boolean notify) {
            if (!values.isEmpty()) {
                for (K value : values) {
                    add(value);
                }
            }

            if (notify) {
                notifyDataSetChanged();
            }
        }

        public void remove(OnCompare<K> compare) {
            for (K value : getOriginal()) {
                if (compare.execute(value)) {
                    remove(value);
                    return;
                }
            }
        }

        public void remove(int position) {
            remove(position, false);
        }

        public void remove(int position, boolean notify) {
            if (position >= original.size()) {
                return;
            }

            original.remove(position);
            if (notify) {
                notifyItemRemoved(position);
            }
        }

        public void remove(@NonNull K value) {
            remove(value, false);
        }

        public boolean remove(@NonNull K value, boolean notify) {
            int index = 0;
            boolean result = false;

            Iterator<K> data = original.iterator();
            while (data.hasNext()) {
                K adapter = data.next();
                if (adapter.compareTo(value)) {
                    data.remove();
                    if (notify) {
                        notifyItemRemoved(index);
                    }
                    result = true;
                }

                index = index + 1;
            }
            return result;
        }

        public void clear() {
            original.clear();
        }

        public List<K> getOriginal() {
            return original;
        }

        public boolean isEmpty() {
            return original.isEmpty();
        }

        protected void sort(Comparator<K> comparator) {
            if (!isEmpty()) {
                Collections.sort(original, comparator);
            }
        }

        public void showMessageEmpty(@NonNull View view) {
            showMessageEmpty(view, 0, 0);
        }

        public void showMessageEmpty(@NonNull View view, @StringRes int message) {
            showMessageEmpty(view, message, 0);
        }

        public void showMessageEmpty(@NonNull View view, @StringRes int message, @DrawableRes int icon) {
            RelativeLayout empty = view.findViewById(R.id.empty);
            if (empty != null) {
                empty.setVisibility(isEmpty() ? View.VISIBLE : View.GONE);
            }

            if (isEmpty()) {
                TextView container = view.findViewById(R.id.message);
                if (message != 0) {
                    container.setText(view.getContext().getString(message));
                }

                if (icon != 0) {
                    container.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0);
                }
            }
        }

        public RecyclerView startAdapter(@NonNull View view, @NonNull LinearLayoutManager linearLayoutManager) {
            RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(this);

            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
            itemAnimator.setAddDuration(500);
            itemAnimator.setRemoveDuration(500);
            recyclerView.setItemAnimator(itemAnimator);

            FastScroller fastScroller = view.findViewById(R.id.fastscroll);
            if (fastScroller != null) {
                fastScroller.setRecyclerView(recyclerView);
            }

            return recyclerView;
        }

        public RecyclerView startAdapter(@NonNull View view, @NonNull RecyclerView.LayoutManager layoutManager) {
            RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(this);

            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
            itemAnimator.setAddDuration(500);
            itemAnimator.setRemoveDuration(500);
            recyclerView.setItemAnimator(itemAnimator);

            return recyclerView;
        }

        public void setSelectedIds(List<Integer> selectedIds) {
            this.selectedIds = selectedIds;
            notifyDataSetChanged();
        }

    }

    public abstract static class NfcActivity extends Mantum.Activity {

        private static final String TAG = NfcAdapter.class.getSimpleName();

        private static final String TOKEN_KEY = "fqJfdzGDvfwbedsKSUGty3VZ9taXxMVw";

        private static final String MIME_TEXT_PLAIN = "text/plain";

        private static final byte[] PWD = "1234".getBytes();

        private static final byte[] PACK = "cC".getBytes();

        public NfcAdapter getNfcAdapter() {
            return nfcAdapter;
        }

        private NfcAdapter nfcAdapter;

        private NdefMessage ndefMessage;

        private AlertDialog alertDialog;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }

        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            handleIntent(intent);
        }

        @Override
        protected void onResume() {
            super.onResume();
            enableForegroundDispatch();
        }

        @Override
        protected void onPause() {
            super.onPause();
            disableForegroundDispatch();
        }

        @SuppressWarnings("unused")
        @Nullable
        protected String prepareNFCRead(@NonNull Intent intent) {
            return prepareNFCRead(intent, true);
        }

        @Nullable
        protected String prepareNFCRead(@NonNull Intent intent, boolean decrypt) {
            String action = intent.getAction();
            if (action == null || ndefMessage != null) {
                return null;
            }

            return read(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG), decrypt);
        }

        public void prepareNFCWrite(NdefMessage ndefMessage) {
            closeKeyboard();
            if (ndefMessage == null) {
                return;
            }

            this.ndefMessage = ndefMessage;
            View form = View.inflate(this, R.layout.write_data_nfc, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(form);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton(R.string.cancelar, (dialog, which) -> {
                this.ndefMessage = null;
                dialog.dismiss();
            });


            alertDialog = alertDialogBuilder.show();
        }

        private void handleIntent(@Nullable Intent intent) {
            super.setIntent(intent);
            if (intent != null && ndefMessage != null) {
                boolean results = push(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG), ndefMessage);

                ndefMessage = null;
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setPositiveButton(R.string.accept, (dialog, which) -> dialog.dismiss());
                alertDialogBuilder.setMessage(results ? R.string.write_success_nfc : R.string.write_error_nfc);
                alertDialogBuilder.setCancelable(true);
                alertDialogBuilder.show();
            }
        }

        private void enableForegroundDispatch() {
            if (nfcAdapter == null) {
                return;
            }

            try {
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

                IntentFilter[] filters = new IntentFilter[1];
                String[][] techLists = new String[][]{
                        {android.nfc.tech.Ndef.class.getName()},
                        {android.nfc.tech.NdefFormatable.class.getName()}};

                filters[0] = new IntentFilter();
                filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
                filters[0].addCategory(Intent.CATEGORY_DEFAULT);
                filters[0].addDataType(MIME_TEXT_PLAIN);

                nfcAdapter.enableForegroundDispatch(
                        this, pendingIntent, filters, techLists);
            } catch (Exception e) {
                Log.e(TAG, "enableForegroundDispatch: ", e);
            }
        }

        private void disableForegroundDispatch() {
            if (nfcAdapter != null) {
                nfcAdapter.disableForegroundDispatch(this);
            }
        }

        private boolean login(@NonNull MifareUltralight mifareUltralight) {
            try {
                if (!mifareUltralight.isConnected()) {
                    throw new Exception("Error al autenticar al NFC");
                }

                mifareUltralight.transceive(new byte[]{
                        (byte) 0x1B,
                        PWD[0], PWD[1], PWD[2], PWD[3]
                });

                return true;
            } catch (Exception e) {
                Log.e(TAG, "login: ", e);
                return false;
            }
        }

        private boolean write(@NonNull MifareUltralight mifareUltralight, @NonNull NdefMessage ndefMessage) {
            try {
                StringBuilder tagText = new StringBuilder();
                NdefRecord[] records = ndefMessage.getRecords();
                for (NdefRecord ndefRecord : records) {
                    if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                        byte[] payload = ndefRecord.getPayload();

                        int languageCodeLength = payload[0] & 51;
                        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                        tagText.append(new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding));
                    }
                }

                int total = tagText.length();
                for (int i = 0; i < 4; i++) {
                    int page = i + 4;
                    byte[] empty = "    ".getBytes(Charset.forName("UTF-8"));
                    mifareUltralight.writePage(page, empty);

                    int beginIndex = i * 4;
                    if (beginIndex > total) {
                        break;
                    }

                    int endIndex = (i + 1) * 4;
                    if (endIndex > total) {
                        endIndex = total;
                    }

                    StringBuilder text = new StringBuilder(tagText.substring(beginIndex, endIndex));
                    if (text.length() != 4) {
                        for (int j = text.length(); j < 4; j++) {
                            text.append(" ");
                        }
                    }

                    byte[] data = text.toString().getBytes(Charset.forName("UTF-8"));
                    mifareUltralight.writePage(page, data);
                    Log.i(TAG, "write page: " + page);
                }

                return true;
            } catch (Exception e) {
                Log.e(TAG, "write: ", e);
                return false;
            }
        }

        private boolean push(@NonNull Tag tag, @NonNull NdefMessage ndefMessage) {
            try {
                MifareUltralight mifareUltralight = MifareUltralight.get(tag);
                if (mifareUltralight == null) {
                    throw new Exception("El NFC no soporta la tecnología Mifare Ultralight");
                }

                mifareUltralight.connect();
                if (!mifareUltralight.isConnected()) {
                    throw new Exception("No fue posible realizar la conexión con el NFC");
                }

                boolean isLogin = login(mifareUltralight);
                if (!isLogin) {
                    Log.i(TAG, "No fue posible autentiar el NFC");

                    // Se cierra la conexión ya que no es posible volver a registrar el usuario
                    if (mifareUltralight.isConnected()) {
                        mifareUltralight.close();
                    }

                    mifareUltralight.connect();
                    if (!mifareUltralight.isConnected()) {
                        throw new Exception("No fue posible realizar la conexión con el NFC");
                    }

                    boolean isWrite = write(mifareUltralight, ndefMessage);
                    if (!isWrite) {
                        throw new Exception("No fue posible escribir en el NFC");
                    }
                }

                // Escribe en el NFC
                boolean isWrite = write(mifareUltralight, ndefMessage);
                if (!isWrite) {
                    throw new Exception("No fue posible escribir en el NFC");
                }

                // Obtiene la página 2Ah
                byte[] response = mifareUltralight.transceive(new byte[]{
                        (byte) 0x30, // Lectura
                        (byte) 0x2A
                });

                // Configura la etiqueta como protegida contra escritura con intentos de autenticación ilimitados
                if (response != null && response.length >= 16) {
                    int attempts = 0; // Número de intentos
                    mifareUltralight.transceive(new byte[]{
                            (byte) 0xA2, // Escritura
                            (byte) 0x2A,
                            (byte) ((response[0] & 0x078) | (0x000) | (attempts & 0x007)),
                            0, 0, 0
                    });
                }

                response = mifareUltralight.transceive(new byte[]{
                        (byte) 0x30, // Lectura
                        (byte) 0x29
                });

                // Configura la etiqueta para proteger el almacenamiento desde la página 0 y superior
                if (response != null && response.length >= 16) {
                    int auth0 = 0;
                    mifareUltralight.transceive(new byte[]{
                            (byte) 0xA2, // Escritura
                            (byte) 0x29,
                            response[0], 0, response[2],
                            (byte) (auth0 & 0x0ff)
                    });
                }

                // Incluye el token
                mifareUltralight.transceive(new byte[]{
                        (byte) 0xA2,
                        (byte) 0x2C,
                        PACK[0], PACK[1], 0, 0
                });

                // Incluye la contraseña
                mifareUltralight.transceive(new byte[]{
                        (byte) 0xA2,
                        (byte) 0x2B,
                        PWD[0], PWD[1], PWD[2], PWD[3]
                });

                mifareUltralight.close();

                return true;
            } catch (Exception e) {
                Log.e(TAG, "push: ", e);
                return false;
            }
        }

        @Nullable
        private String read(Tag tag, boolean decrypt) {
            try {
                MifareUltralight mifareUltralight = MifareUltralight.get(tag);
                if (mifareUltralight == null) {
                    throw new Exception("mifareUltralight == null");
                }

                SecretKey secretKey = null;
                if (decrypt) {
                    secretKey = generateKey();
                    if (secretKey == null) {
                        throw new Exception("Ocurrio un error generando la llave");
                    }
                }

                mifareUltralight.connect();
                if (!mifareUltralight.isConnected()) {
                    throw new Exception("No fue posible realizar la conexión con el NFC");
                }

                byte[] response = mifareUltralight.readPages(4);
                String value = new String(response, Charset.forName("UTF-8"));

                if (secretKey != null && !value.isEmpty()) {
                    value = decrypt(value, secretKey);
                }

                if (value == null || value.isEmpty()) {
                    throw new Exception("El valor esta vacio");
                }

                return value.trim();
            } catch (Exception e) {
                Log.e(TAG, "read: ", e);
                return null;
            }
        }

        @Nullable
        private static String encrypt(String content, SecretKey secretKey) {
            try {
                if (secretKey == null) {
                    throw new Exception("La clave no esta definida");
                }

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                byte[] iv = new byte[cipher.getBlockSize()];
                SecureRandom random = new SecureRandom();
                random.nextBytes(iv);

                IvParameterSpec ivParams = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

                byte[] cipherText = cipher.doFinal(content.getBytes("UTF-8"));
                byte[] ivAndCipherText = getCombinedArray(iv, cipherText);
                return Base64.encodeToString(ivAndCipherText, Base64.NO_WRAP);
            } catch (Exception e) {
                Log.e(TAG, "encrypt: ", e);
                return null;
            }
        }

        @Nullable
        private static String decrypt(String content, SecretKey secretKey) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                byte[] ivAndCipherText = Base64.decode(content, Base64.NO_WRAP);
                byte[] iv = Arrays.copyOfRange(ivAndCipherText, 0, cipher.getBlockSize());
                byte[] cipherText = Arrays.copyOfRange(ivAndCipherText,
                        cipher.getBlockSize(), ivAndCipherText.length);

                IvParameterSpec ivParams = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);

                return new String(cipher.doFinal(cipherText), "UTF-8");
            } catch (Exception e) {
                Log.e(TAG, "encrypt: ", e);
                return null;
            }
        }

        @Nullable
        private static SecretKeySpec generateKey() {
            try {
                return new SecretKeySpec(TOKEN_KEY.getBytes("UTF-8"), "AES");
            } catch (Exception e) {
                Log.e(TAG, "generateKey: ", e);
                return null;
            }
        }

        private static byte[] getCombinedArray(byte[] one, byte[] two) {
            byte[] combined = new byte[one.length + two.length];
            for (int i = 0; i < combined.length; ++i) {
                combined[i] = i < one.length ? one[i] : two[i - one.length];
            }
            return combined;
        }

        @Nullable
        public static NdefMessage write(@NonNull String content) {
            return write(content, true);
        }

        @Nullable
        public static NdefMessage write(@NonNull String content, boolean encrypt) {
            try {
                byte[] language = Locale.getDefault()
                        .getLanguage().getBytes("UTF-8");

                if (encrypt) {
                    content = encrypt(content, generateKey());
                    if (content == null) {
                        throw new Exception("Ocurrio un error al encriptar el contenido");
                    }
                }

                byte[] text = content.getBytes("UTF-8");
                int languageSize = language.length;
                int textLength = text.length;

                ByteArrayOutputStream payload
                        = new ByteArrayOutputStream(1 + languageSize + textLength);
                payload.write((byte) (languageSize & 0x1F));
                payload.write(language, 0, languageSize);
                payload.write(text, 0, textLength);

                NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                        NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
                return new NdefMessage(new NdefRecord[]{ndefRecord});
            } catch (Exception e) {
                Log.e(TAG, "write: ", e);
                return null;
            }
        }
    }

    public static String toPrettyFormat(String jsonString) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonString)
                    .getAsJsonObject();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .setPrettyPrinting()
                    .create();
            return gson.toJson(json);
        } catch (Exception e) {
            return jsonString;
        }

    }

    public static void applyTouchListener(final View v, final ScrollView scrollView) {
        /*
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

         */
    }
}