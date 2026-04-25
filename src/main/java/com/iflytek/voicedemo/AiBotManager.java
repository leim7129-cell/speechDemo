package com.iflytek.voicedemo;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AiBotManager {
    private static final String TAG = "AiBotManager";
    
    // DeepSeek API 配置 (请在此处填入您的 API Key)
    private static final String API_KEY = "sk-3d0737101a4b44c4af7007ce4a8458fd";
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    public enum Language {
        CN, EN
    }

    private Scenario currentScenario;
    public Language currentLanguage = Language.EN;

    public interface AiCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void setScenario(Scenario scenario) {
        this.currentScenario = scenario;
    }

    public void setLanguage(Language language) {
        this.currentLanguage = language;
    }

    /**
     * 调用 DeepSeek API 获取响应
     */
    public void getResponseAsync(String userText, AiCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setDoOutput(true);

                // 构建请求 Body (OpenAI 格式)
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "deepseek-chat");
                
                JSONArray messages = new JSONArray();
                
                // 设置系统提示词 (System Prompt) 
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                
                String prompt = "";
                if (currentScenario != null) {
                    prompt = currentScenario.getSystemPrompt();
                } else {
                    if (currentLanguage == Language.EN) {
                        prompt = "You are a helpful English tutor. Chat with the user in daily English. Keep your answers concise and natural.";
                    } else {
                        prompt = "你是一个友好的中文老师。请用日常中文与用户交流，回答要简洁自然。";
                    }
                }
                
                systemMsg.put("content", prompt);
                messages.put(systemMsg);

                // 用户消息
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userText);
                messages.put(userMsg);

                requestBody.put("messages", messages);
                requestBody.put("stream", false);

                // 发送请求
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // 读取响应
                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    
                    // 解析 DeepSeek 返回的 JSON
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String aiContent = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    
                    callback.onResponse(aiContent);
                } else {
                    callback.onError("HTTP Error: " + code);
                }
            } catch (Exception e) {
                Log.e(TAG, "DeepSeek API call failed", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }
}

