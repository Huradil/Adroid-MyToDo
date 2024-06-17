package com.project.mytodo.screens.details;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.appcompat.widget.Toolbar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.project.mytodo.App;
import com.project.mytodo.R;
import com.project.mytodo.model.Note;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE = "NoteDetailsActivity.EXTRA_NOTE";
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1;

    private Note note;

    private EditText editText;

    private EditText endTimeEditText;

    public static void start(Activity caller, Note note) {
        Intent intent = new Intent(caller, NoteDetailsActivity.class);
        if (note != null) {
            intent.putExtra(EXTRA_NOTE, note);
        }
        caller.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setTitle(getString(R.string.note_details_title));

        editText = findViewById(R.id.text);
        endTimeEditText = findViewById(R.id.end_time);

        if (getIntent().hasExtra(EXTRA_NOTE)) {
            note = getIntent().getParcelableExtra(EXTRA_NOTE);
            editText.setText(note.text);
            if (note.endTime > 0) {
                endTimeEditText.setText(formatDateTime(note.endTime));
            }
        } else {
            note = new Note();
        }
        endTimeEditText.setOnClickListener(v -> showDateTimePicker());

        // Запрос разрешения на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
            }
        }
    }
    private void showDateTimePicker() {
        // Получение текущей даты и времени
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();
        new DatePickerDialog(NoteDetailsActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(NoteDetailsActivity.this, (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                endTimeEditText.setText(formatDateTime(date.getTimeInMillis()));
                note.endTime = date.getTimeInMillis();
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private String formatDateTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.action_save) {
            if (editText.getText().length() > 0) {
                note.text = editText.getText().toString();
                note.done = false;
                note.timestamp = System.currentTimeMillis();

                //проверяем и устанавливаем занчение endTime
                if (!endTimeEditText.getText().toString().isEmpty()) {
                    try {
                       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                       Date date = sdf.parse(endTimeEditText.getText().toString());
                       if (date != null) {
                           note.endTime = date.getTime();
                       }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (getIntent().hasExtra(EXTRA_NOTE)) {
                    App.getInstance().getNoteDao().update(note);
                } else {
                    App.getInstance().getNoteDao().insert(note);
                }
                scheduleNotifications(note);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void scheduleNotifications(Note note) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long tenHoursBefore = note.endTime - 10 * 60 * 60 * 1000;
        long fiveHoursBefore = note.endTime - 5 * 60 * 60 * 1000;
        long oneHourBefore = note.endTime - 60 * 60 * 1000;

        // Создание интентов для каждого уведомления
        scheduleNotification(alarmManager, tenHoursBefore, note.text, 0);
        scheduleNotification(alarmManager, fiveHoursBefore, note.text, 1);
        scheduleNotification(alarmManager, oneHourBefore, note.text, 2);
    }

    private void scheduleNotification(AlarmManager alarmManager, long time, String noteText, int requestCode) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("note_text", noteText);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (time > System.currentTimeMillis()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }

}