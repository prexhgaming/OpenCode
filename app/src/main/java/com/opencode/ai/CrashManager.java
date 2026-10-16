package com.opencode.ai;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Best-effort fatal exception and ANR reporting for the app process.
 * Install from Application.onCreate(). No handler can recover from native
 * crashes, SIGKILL, or an OOM that prevents even minimal allocations.
 */
public final class CrashManager {

    private static final String TAG = "OpenCodeCrash";
    private static final String PREFS = "crash_reporter";
    private static final String KEY_LOG = "last_log";
    private static final long ANR_TIMEOUT_MS = 5000L;
    private static volatile Application application;
    private static volatile Thread.UncaughtExceptionHandler previousHandler;
    private static final AtomicBoolean reporting = new AtomicBoolean(false);

    private CrashManager() {}

    public static void install(Application app) {
        application = app;
        previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            String log = format("UNCAUGHT EXCEPTION", thread, error);
            saveLog(app, log);
            launchCrashActivity(app, log);
            // Keep standard Android crash semantics after giving the report
            // screen a chance to launch. A fatal thread cannot safely continue.
            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, error);
            } else {
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        });
        startAnrWatchdog(app);
    }

    /** Call this from catch blocks for handled IO, database, or other failures. */
    public static void reportException(Context context, String category, Throwable error) {
        String log = format(category == null ? "REPORTED ERROR" : category,
                Thread.currentThread(), error);
        saveLog(context.getApplicationContext(), log);
        launchCrashActivity(context.getApplicationContext(), log);
    }

    public static void reportMessage(Context context, String category, String message) {
        String log = "[" + category + "]\n" + String.valueOf(message);
        saveLog(context.getApplicationContext(), log);
        launchCrashActivity(context.getApplicationContext(), log);
    }

    static String getLastCrashLog(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_LOG, "No crash log is available.");
    }

    private static String format(String category, Thread thread, Throwable error) {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        writer.println("OpenCode Android error report");
        writer.println("Category: " + category);
        writer.println("Time: " + new java.util.Date());
        writer.println("Thread: " + (thread == null ? "unknown" : thread.getName()));
        writer.println("Android: " + android.os.Build.VERSION.RELEASE
                + " (SDK " + android.os.Build.VERSION.SDK_INT + ")");
        writer.println("Device: " + android.os.Build.MANUFACTURER + " "
                + android.os.Build.MODEL);
        writer.println();
        if (error != null) error.printStackTrace(writer);
        writer.flush();
        return buffer.toString();
    }

    private static void saveLog(Context context, String log) {
        try {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putString(KEY_LOG, log).commit();
        } catch (Throwable ignored) {
            Log.e(TAG, "Could not persist crash log", ignored);
        }
        Log.e(TAG, log);
    }

    private static void launchCrashActivity(Context context, String log) {
        if (!reporting.compareAndSet(false, true)) return;
        try {
            Intent intent = new Intent(context, CrashActivity.class);
            intent.putExtra(CrashActivity.EXTRA_LOG, log);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (Throwable launchFailure) {
            Log.e(TAG, "Could not launch crash activity", launchFailure);
        }
    }

    private static void startAnrWatchdog(Application app) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Thread watchdog = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                final AtomicBoolean responded = new AtomicBoolean(false);
                mainHandler.post(() -> responded.set(true));
                SystemClock.sleep(ANR_TIMEOUT_MS);
                if (!responded.get()) {
                    String log = "OpenCode Android error report\n"
                            + "Category: ANR / Main thread unresponsive\n"
                            + "Time: " + new java.util.Date() + "\n"
                            + "The main thread did not respond within "
                            + ANR_TIMEOUT_MS + " ms.\n"
                            + "This indicates a possible deadlock or long-running "
                            + "operation on the UI thread.\n";
                    saveLog(app, log);
                    launchCrashActivity(app, log);
                    // Report once; keep checking in case the main thread recovers.
                    SystemClock.sleep(ANR_TIMEOUT_MS);
                }
            }
        }, "OpenCode-ANR-Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }
}
