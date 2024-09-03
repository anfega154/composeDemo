package com.mantum.component.service;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.util.Log;

import com.mantum.component.R;

import java.util.TimeZone;

import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.provider.CalendarContract.*;

public class Calendar {

    private static final String TAG = Calendar.class.getSimpleName();

    private static final String[] EVENT_PROJECTION = new String[]{
            Calendars._ID,
            Calendars.ACCOUNT_NAME,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.OWNER_ACCOUNT
    };

    private static final int CALENDAR_PERMISSION_REQUEST_CODE = 1;

    private final Context context;

    private final ContentResolver contentResolver;

    public Calendar(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
    }

    public static boolean requestPermission(@NonNull Context context) {
        if ((ActivityCompat.checkSelfPermission(context, WRITE_CALENDAR) != PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(context, READ_CALENDAR) != PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{WRITE_CALENDAR, READ_CALENDAR}, CALENDAR_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    public static boolean checkPermission(@NonNull Context context) {
        return ActivityCompat.checkSelfPermission(context, WRITE_CALENDAR) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, READ_CALENDAR) == PERMISSION_GRANTED;
    }

    public static void open(@NonNull Context context) {
        try {
            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, java.util.Calendar.getInstance().getTimeInMillis());
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(builder.build());
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "open: ", e);
        }
    }

    @Nullable
    public Cursor find(@NonNull String owner) {
        if (!checkPermission(context)) {
            return null;
        }

        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[]{context.getString(R.string.calendar_name), ACCOUNT_TYPE_LOCAL, owner};

        return contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
    }

    public Long createNew(@NonNull String displayName, @NonNull String owner) {
        return createNew(displayName, owner, Color.parseColor("#e64d00"));
    }

    public Long createNew(@NonNull String displayName, @NonNull String owner, int color) {
        try {
            String accountName = context.getString(R.string.calendar_name);

            ContentValues contentValues = new ContentValues();
            contentValues.put(Calendars.ACCOUNT_NAME, accountName);
            contentValues.put(Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL);
            contentValues.put(Calendars.NAME, displayName);
            contentValues.put(Calendars.CALENDAR_DISPLAY_NAME, displayName);
            contentValues.put(Calendars.CALENDAR_COLOR, color);
            contentValues.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
            contentValues.put(Calendars.OWNER_ACCOUNT, owner);
            contentValues.put(Calendars.VISIBLE, 1);
            contentValues.put(Calendars.SYNC_EVENTS, 1);

            Uri uri = asSyncAdapter(Calendars.CONTENT_URI, ACCOUNT_TYPE_LOCAL);
            Uri result = contentResolver.insert(uri, contentValues);
            if (result == null) {
                return null;
            }

            Log.i("Calendar", "Create new calendar [" + displayName + "]");
            return Long.parseLong(result.getLastPathSegment());
        } catch (Exception e) {
            Log.e(TAG, "createNew: ", e);
            return null;
        }
    }

    @Nullable
    public Long createEvent(@NonNull Long id, @NonNull Event event) {
        try {
            if (!checkPermission(context)) {
                return null;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(Events.DTSTART, event.getBegin());
            contentValues.put(Events.DTEND, event.getEnd());
            contentValues.put(Events.TITLE, event.getTitle());
            contentValues.put(Events.DESCRIPTION, event.getDescription());
            contentValues.put(Events.CALENDAR_ID, id);
            // contentValues.put(Events.EVENT_COLOR, event.getColor()); TODO: ERROR CON EL FORMATO DEL COLOR
            contentValues.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            if (event.isDiaCompleto()) {
                contentValues.put(Events.ALL_DAY, 1);
            }

            Uri uri = contentResolver.insert(Events.CONTENT_URI, contentValues);
            if (uri == null) {
                return null;
            }

            return Long.parseLong(uri.getLastPathSegment());
        } catch (Exception e) {
            Log.e(TAG, "createEvent: ", e);
            return null;
        }
    }

    public boolean updateEvent(@NonNull Long id, @NonNull Event event) {
        try {
            if (!checkPermission(context)) {
                return false;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(Events.DTSTART, event.getBegin());
            contentValues.put(Events.DTEND, event.getEnd());
            contentValues.put(Events.TITLE, event.getTitle());
            contentValues.put(Events.DESCRIPTION, event.getDescription());
            contentValues.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
            if (event.isDiaCompleto()) {
                contentValues.put(Events.ALL_DAY, 1);
            }

            if (contentResolver != null) {
                Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
                contentResolver.update(updateUri, contentValues, null, null);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteEvent(@NonNull Long id) {
        if (contentResolver != null) {
            Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
        }
    }

    private static Uri asSyncAdapter(@NonNull Uri uri, String accountType) {
        return uri.buildUpon()
                .appendQueryParameter(Calendars.ACCOUNT_NAME, "com.mantum")
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType)
                .appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
                .build();
    }
}