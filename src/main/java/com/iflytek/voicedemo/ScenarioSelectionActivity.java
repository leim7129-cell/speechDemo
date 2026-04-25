package com.iflytek.voicedemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ScenarioSelectionActivity extends AppCompatActivity {

    private RecyclerView rvScenarios;
    private ScenarioAdapter adapter;
    private ImageButton btnAdd;
    private List<Scenario> scenarioList;
    private android.view.View navEvaluation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scenario_selection);

        // 确保 MSC 初始化，防止直接启动此 Activity 时崩溃
        SpeechApp.initializeMsc(this);

        rvScenarios = findViewById(R.id.rv_scenarios);
        btnAdd = findViewById(R.id.btn_add_scenario);
        navEvaluation = findViewById(R.id.nav_evaluation);

        scenarioList = new ArrayList<>(ScenarioManager.getScenarios());
        adapter = new ScenarioAdapter(this, scenarioList, scenario -> {
            Intent intent = new Intent(ScenarioSelectionActivity.this, AiChatActivity.class);
            intent.putExtra("scenario", scenario);
            startActivity(intent);
        });

        rvScenarios.setLayoutManager(new GridLayoutManager(this, 2));
        rvScenarios.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            showAddScenarioDialog();
        });

        navEvaluation.setOnClickListener(v -> {
            Intent intent = new Intent(ScenarioSelectionActivity.this, EvaluationActivity.class);
            startActivity(intent);
        });
    }

    private void showAddScenarioDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加新场景");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("场景名称 (如: 咖啡店点餐)");
        layout.addView(etTitle);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("场景描述 (如: 在咖啡店练习点单)");
        layout.addView(etDesc);

        final EditText etPrompt = new EditText(this);
        etPrompt.setHint("AI 角色设定 (Prompt)");
        layout.addView(etPrompt);

        final EditText etWelcome = new EditText(this);
        etWelcome.setHint("开场白");
        layout.addView(etWelcome);

        builder.setView(layout);

        builder.setPositiveButton("添加", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String desc = etDesc.getText().toString();
            String prompt = etPrompt.getText().toString();
            String welcome = etWelcome.getText().toString();

            if (!title.isEmpty()) {
                Scenario newScenario = new Scenario(
                    "custom_" + System.currentTimeMillis(),
                    title,
                    desc,
                    "ic_chat",
                    prompt,
                    welcome
                );
                scenarioList.add(newScenario);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "场景已添加", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
