package com.mantum.component.service;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.View;

import com.mantum.component.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

public class Camera {

    private static final String TAG = Camera.class.getSimpleName();

    public static final int REQUEST_TAKE_PHOTO = 1;

    public static final int CAMERA_PERMISSION_REQUEST_CODE = 2;

    private String path;

    private Uri photoURI;

    private final View view;

    private final Context context;

    public Camera(@NonNull Context context) {
        this.context = context;
        this.view = ((Activity) context).findViewById(android.R.id.content);
    }

    public void capture() {
        try {
            if (!isCheckCamera()) {
                Snackbar.make(view, R.string.camera_check, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            if (!checkPermission(context)) {
                Snackbar.make(view, R.string.camera_permission, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) == null) {
                Snackbar.make(view, R.string.camera_connect, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            File photoFile = null;
            try {
                photoFile = createImage();
            } catch (IOException ignored) {
            }

            if (photoFile == null) {
                Snackbar.make(view, R.string.camera_file, Snackbar.LENGTH_LONG)
                        .show();
                return;
            }

            if (photoURI != null) {
                Log.i(TAG, "capture: Remember revoke permission camera.revokePermission()");
                revokePermission();
            }

            photoURI = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", photoFile);
            grantUriPermission(takePictureIntent, photoURI);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        } catch (Exception e) {
            Log.e(TAG, "capture: ", e);
            Snackbar.make(view, R.string.camera_connect, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private boolean isCheckCamera() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @Nullable
    @SuppressWarnings("unused")
    public Photo includeGallery() {
        return includeGallery(null);
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public Photo includeGallery(Location location) {
        return includeGallery(location, null);
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public Photo includeGallery(Location location, Integer idCategory) {
        return includeGallery(location, idCategory, true);
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public Photo includeGallery(Location location, Integer idCategory, boolean compress) {
        if (path == null) {
            Snackbar.make(view, R.string.camera_path_error, Snackbar.LENGTH_LONG)
                    .show();
            return null;
        }

        File file = new File(path);
        if (compress) {
            boolean result = compress(file);
            if (!result) {
                Log.e(TAG, "No fue posible comprimir la foto");
            }
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        context.sendBroadcast(mediaScanIntent);

        if (location != null) {
            boolean include = includeLocation(file, location);
            if (!include) {
                Snackbar.make(view, R.string.camera_location, Snackbar.LENGTH_LONG)
                        .show();
            }
        }

        return new Photo(context, file, idCategory);
    }

    @SuppressWarnings("WeakerAccess")
    public void revokePermission() {
        if (photoURI != null) {
            context.revokeUriPermission(photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            photoURI = null;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean requestPermission(@NonNull Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean checkPermission(@NonNull Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void grantUriPermission(@NonNull Intent intent, @NonNull Uri photo) {
        List<ResolveInfo> resolvedIntentActivities = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            context.grantUriPermission(resolvedIntentInfo.activityInfo.packageName,
                    photo, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    private boolean includeLocation(@NonNull File file, @NonNull Location location) {
        try {
            SimpleDateFormat simpleDateFormat
                    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String datetime = simpleDateFormat.format(java.util.Calendar.getInstance().getTime());

            SimpleDateFormat simpleDateGPSFormat
                    = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = simpleDateGPSFormat.format(location.getTime());

            SimpleDateFormat simpleTimeGPSFormat
                    = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
            String time = simpleTimeGPSFormat.format(location.getTime());

            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, datetime);
            exifInterface.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, date);
            exifInterface.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, time);
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                    convert(location.getLatitude()));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
                    location.getLatitude() < 0 ? "S" : "N");
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                    convert(location.getLongitude()));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
                    location.getLongitude() < 0 ? "W" : "E");
            exifInterface.saveAttributes();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "includeLocation: ", e);
            return false;
        }
    }

    @Nullable
    public static String convert(double value) {
        try {
            String[] degMinSec = convertCoordinateToSeconds(Math.abs(value))
                    .split(":");

            String seconds = degMinSec[2];
            if (seconds.contains(".")) {
                seconds = seconds.replace(".", ",");
            }

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            symbols.setGroupingSeparator(' ');

            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setDecimalFormatSymbols(symbols);

            double deg = 0d;
            Number parse = decimalFormat.parse(seconds);
            if (parse != null) {
                deg = parse.doubleValue();
            }

            return degMinSec[0] + "/1," + degMinSec[1] + "/1," + (int) (deg * 1000) + "/1000";
        } catch (Exception e) {
            Log.e(TAG, "convert: ", e);
            return null;
        }
    }

    @NonNull
    private static String convertCoordinateToSeconds(double coordinate) {
        if (coordinate < -180.0 || coordinate > 180.0 ||
                Double.isNaN(coordinate)) {
            throw new IllegalArgumentException("coordinate=" + coordinate);
        }

        StringBuilder sb = new StringBuilder();
        if (coordinate < 0) {
            sb.append('-');
            coordinate = -coordinate;
        }

        DecimalFormat df = new DecimalFormat("###.#####");
        int degrees = (int) Math.floor(coordinate);
        sb.append(degrees);
        sb.append(':');
        coordinate -= degrees;
        coordinate *= 60.0;

        int minutes = (int) Math.floor(coordinate);
        sb.append(minutes);
        sb.append(':');
        coordinate -= minutes;
        coordinate *= 60.0;

        sb.append(df.format(coordinate));
        return sb.toString();
    }

    @NonNull
    private File createImage() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        path = image.getAbsolutePath();
        return image;
    }

    private boolean compress(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            Bitmap decode = ScalingUtilities.decodeResource(file.getAbsolutePath(),
                    options.outWidth, options.outHeight,
                    ScalingUtilities.ScalingLogic.FIT);

            decode = modifyOrientation(decode, file);
            Bitmap bitmap = ScalingUtilities.createScaledBitmap(decode,
                    options.outWidth, options.outHeight,
                    ScalingUtilities.ScalingLogic.FIT);
            decode.recycle();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "compress: ", e);
            return false;
        }
    }

    private Bitmap modifyOrientation(@NonNull Bitmap bitmap, @NonNull File file) throws IOException {
        ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    private Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}