package com.powerlevel.saiyan;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton;

public class MainActivity extends Activity {
  private android.content.SharedPreferences prefs;
  private TextView xpText, statusText;
  private CheckBox stepsCheck, proteinCheck;
  private Button workoutButton;
  private int xp;
  private boolean workoutDone;
  private void updateXp() { xp = prefs.getInt("xp", 0); xpText.setText("LEVEL " + (1 + xp / 500) + " • " + xp + " XP"); workoutDone = prefs.getBoolean("workout", false); workoutButton.setText(workoutDone ? "WORKOUT COMPLETE ✓" : "COMPLETE TODAY'S WORKOUT  +100 XP"); }
  @Override public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); setContentView(R.layout.activity_main); prefs = getSharedPreferences("saiyan", MODE_PRIVATE); xpText = findViewById(R.id.xpText); statusText = findViewById(R.id.statusText); stepsCheck = findViewById(R.id.stepsCheck); proteinCheck = findViewById(R.id.proteinCheck); workoutButton = findViewById(R.id.workoutButton); EditText weightInput = findViewById(R.id.weightInput); Button saveWeight = findViewById(R.id.saveWeightButton); stepsCheck.setOnCheckedChangeListener((button, checked) -> { prefs.edit().putBoolean("steps", checked).apply(); addMissionXp("steps", checked, 50); }); proteinCheck.setOnCheckedChangeListener((button, checked) -> { prefs.edit().putBoolean("protein", checked).apply(); addMissionXp("protein", checked, 50); }); stepsCheck.setChecked(prefs.getBoolean("steps", false)); proteinCheck.setChecked(prefs.getBoolean("protein", false)); updateXp(); workoutButton.setOnClickListener(v -> { if (!prefs.getBoolean("workout", false)) { prefs.edit().putBoolean("workout", true).putInt("xp", prefs.getInt("xp",0)+100).apply(); updateXp(); statusText.setText("Mission complete! Great work, Saiyan. Prioritize sleep, food, and recovery."); } else { statusText.setText("Workout already logged today. Recover and come back tomorrow!"); } }); saveWeight.setOnClickListener(v -> { String w = weightInput.getText().toString().trim(); if (!w.isEmpty()) { prefs.edit().putString("weight", w).apply(); statusText.setText("Check-in saved: " + w + " lb. Track weekly trends, not daily fluctuations."); } else statusText.setText("Enter your weight in pounds first."); }); String saved = prefs.getString("weight", ""); if (!saved.isEmpty()) weightInput.setText(saved); }
  private void addMissionXp(String key, boolean checked, int amount) { boolean was = prefs.getBoolean(key + "Awarded", false); if (checked && !was) { prefs.edit().putInt("xp", prefs.getInt("xp",0)+amount).putBoolean(key+"Awarded", true).apply(); } else if (!checked && was) { prefs.edit().putInt("xp", Math.max(0,prefs.getInt("xp",0)-amount)).putBoolean(key+"Awarded", false).apply(); } updateXp(); }
}
