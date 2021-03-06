package org.fossasia.openevent.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.fossasia.openevent.data.Session;
import org.fossasia.openevent.receivers.NotificationAlarmReceiver;
import org.threeten.bp.ZonedDateTime;

import io.reactivex.Completable;
import timber.log.Timber;

public class NotificationUtil {

    private static void onSuccess(Session session) {
        Timber.d("Created notification for Session %d %s at time %s",
                session.getId(), session.getTitle(), session.getStartsAt());
    }

    private static void onError(Throwable throwable, Session session) {
        Timber.e(throwable);
        Timber.e("Error creating Date for Session %ld %s at time %s",
                session.getId(), session.getTitle(), session.getStartsAt());
    }

    public static Completable createNotification(Session session, Context context) {
        return Completable.fromAction(() -> {
            ZonedDateTime zonedDateTime = DateConverter.getDate(session.getStartsAt());

            Integer pref_result = Integer.parseInt(SharedPreferencesUtil.getString("notification", "10 mins").substring(0, 2).trim());
            if (pref_result.equals(1)) {
                zonedDateTime.minusHours(-1);
            } else if (pref_result.equals(12)) {
                zonedDateTime.minusHours(12);
            } else {
                zonedDateTime.minusHours(10);
            }
            Intent myIntent = new Intent(context, NotificationAlarmReceiver.class);
            myIntent.putExtra(ConstantStrings.SESSION, session.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, zonedDateTime.toInstant().toEpochMilli(), pendingIntent);
        }).doOnComplete(() -> onSuccess(session)).doOnError(throwable -> onError(throwable, session));
    }
}
