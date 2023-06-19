package com.example.avocado;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExpiryDateNotifier {
/*
    private static final String CHANNEL_ID = "expiry_date_channel";
    private static final int NOTIFICATION_ID = 1;
    private final Context context;

    public ExpiryDateNotifier(Context context) {
        this.context = context;
    }

    // 모든 냉장고의 식재료의 유통기한을 확인하고 알림을 생성하는 메서드
    public void checkExpiryDates() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference refrigeratorNamesRef = db.collection("RefrigeratorName");

        refrigeratorNamesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> expiredFoods = new ArrayList<>(); // 유통기한이 임박한 식재료를 저장할 리스트 생성

            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String fridgeName = documentSnapshot.getId();
                CollectionReference fridgeRef = db.collection(fridgeName);
                Query query = fridgeRef.orderBy("expiryDate");

                query.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1) {
                        String foodName = documentSnapshot1.getId();
                        String expiryDate = documentSnapshot1.getString("expiryDate");
                        if (expiryDate != null) {
                            Date expiry = parseDateString(expiryDate);
                            Calendar current = Calendar.getInstance();
                            current.setTimeInMillis(System.currentTimeMillis());

                            long diffInMilliseconds = expiry.getTime() - current.getTimeInMillis();
                            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMilliseconds);

                            if ((diffInDays < 3) && (diffInDays >= 0)) {
                                expiredFoods.add(foodName); // 유통기한이 임박한 식재료를 리스트에 추가
                            }
                        }
                    }

                    if (!expiredFoods.isEmpty()) {
                        createNotification(expiredFoods); // 유통기한이 임박한 식재료 리스트로 알림 생성
                    }
                }).addOnFailureListener(Throwable::printStackTrace);
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    // String 형태의 날짜를 Date 객체로 변환하는 메서드
    private Date parseDateString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 알림에 표시할 텍스트 생성 메서드
    private String getNotificationText(List<String> expiredFoods) {
        StringBuilder builder = new StringBuilder();
        int size = expiredFoods.size();
        for (int i = 0; i < size; i++) {
            builder.append(expiredFoods.get(i));
            if (i < size - 1) {
                builder.append(", ");
            }
        }
        builder.append("의 유통기한이 임박했어요!");
        return builder.toString();
    }

    // 알림을 생성하는 메서드
    private void createNotification(List<String> expiredFoods) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O 이상에서는 채널을 생성해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Expiry Date Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // 알림 클릭 시 실행할 동작을 정의하는 Intent 생성
        Intent intent = new Intent(context, MainActivity.class);

        // 알림 빌더 생성
        String notificationText = getNotificationText(expiredFoods); // 알림 텍스트 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("유통기한 알림")
                .setContentText(notificationText)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 알림 표시
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // 알림 권한이 허용되었는지 확인하는 메서드
    private boolean isNotificationPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NOTIFICATION_POLICY)
                == PackageManager.PERMISSION_GRANTED;
    }

    // 알림 권한 요청 다이얼로그를 보여주는 메서드
    private void showNotificationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("알림 권한 요청")
                .setMessage("알림을 받기 위해서는 알림 권한이 필요합니다. 설정에서 알림 권한을 허용해주세요.")
                .setPositiveButton("설정으로 이동", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("거부", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPermissionDenied();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // 앱 설정 화면으로 이동하는 메서드
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        ((Activity) context).startActivityForResult(intent, 0);
    }

    private void onPermissionGranted() {
        Toast.makeText(context, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void onPermissionDenied() {
        Toast.makeText(context, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
    }*/
}
