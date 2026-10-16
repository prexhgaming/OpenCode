package com.opencode.ai;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.opencode.ai.databinding.ActivityCrashBinding;

/** AppCompat crash screen, XML layout accessed via View Binding. */
public final class CrashActivity extends AppCompatActivity {
    public static final String EXTRA_LOG = "com.opencode.ai.extra.CRASH_LOG";

    private ActivityCrashBinding binding;
    private String crashLog;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityCrashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Wire up the MaterialToolbar as the support action bar.
        setSupportActionBar(binding.crashToolbar);

        crashLog = getIntent().getStringExtra(EXTRA_LOG);
        if (crashLog == null || crashLog.trim().isEmpty()) {
            crashLog = CrashManager.getLastCrashLog(this);
        }
        if (crashLog == null || crashLog.trim().isEmpty()) {
            crashLog = "No crash log is available.";
        }

        binding.crashToolbar.setNavigationOnClickListener(v -> restartApp());
        binding.crashToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_copy_crash_log) {
                copyLog();
                return true;
            }
            return false;
        });

        // Keep the report scrollable but prevent selecting/copying text directly.
        binding.crashLog.setText(crashLog);
        binding.crashLog.setTextIsSelectable(false);
        binding.crashLog.setLongClickable(false);
        binding.crashLog.setOnLongClickListener(v -> true);
    }

    private void copyLog() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Crash log", crashLog));
            Toast.makeText(this, "Crash log copied", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Clipboard unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void restartApp() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(launch);
        }
        finishAffinity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onBackPressed() {
        restartApp();
    }
}