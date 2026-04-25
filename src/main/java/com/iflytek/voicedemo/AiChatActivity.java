package com.iflytek.voicedemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.speech.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AiChatActivity extends Activity {
    private ListView mListView;
    private Button mBtnVoice;
    private Button mBtnLanguage;
    private TextView mTvTitle;
    private List<ChatMessage> mMessages = new ArrayList<>();
    private ChatAdapter mAdapter;
    
    private AiBotManager mBotManager;
    private SpeechRecognizer mIat;
    private SpeechSynthesizer mTts;
    
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private Scenario mScenario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ai_chat);

        // 确保 MSC 初始化
        SpeechApp.initializeMsc(this);

        mScenario = (Scenario) getIntent().getSerializableExtra("scenario");
        mBotManager = new AiBotManager();
        if (mScenario != null) {
            mBotManager.setScenario(mScenario);
        }
        
        initUI();
        initSpeech();
        
        if (mScenario != null && mScenario.getWelcomeMessage() != null) {
            addMessage(mScenario.getWelcomeMessage(), false);
            // Proactively speak the welcome message
            new android.os.Handler().postDelayed(() -> {
                if (mTts != null) {
                    mTts.startSpeaking(mScenario.getWelcomeMessage(), null);
                }
            }, 500);
        } else {
            addMessage("Hi! I'm your AI tutor. Click the button to talk to me!", false);
        }
    }

    private void initUI() {
        mListView = findViewById(R.id.chat_list_view);
        mBtnVoice = findViewById(R.id.btn_voice_input);
        mBtnLanguage = findViewById(R.id.btn_switch_language);
        mTvTitle = findViewById(R.id.chat_title);
        
        if (mScenario != null && mTvTitle != null) {
            mTvTitle.setText(mScenario.getTitle());
        }

        mAdapter = new ChatAdapter();
        mListView.setAdapter(mAdapter);

        if (mBtnLanguage != null) {
            mBtnLanguage.setOnClickListener(v -> {
                if (mBotManager.currentLanguage == AiBotManager.Language.EN) {
                    mBotManager.setLanguage(AiBotManager.Language.CN);
                    mBtnLanguage.setText("语言: CN");
                    addMessage("已切换到中文模式", false);
                } else {
                    mBotManager.setLanguage(AiBotManager.Language.EN);
                    mBtnLanguage.setText("语言: EN");
                    addMessage("Switched to English mode", false);
                }
                updateSpeechParams();
            });
        }

        View btnSwitchScene = findViewById(R.id.btn_switch_scene);
        if (btnSwitchScene != null) {
            btnSwitchScene.setVisibility(View.GONE);
        }

        mBtnVoice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startIat();
                    mBtnVoice.setText(mBotManager.currentLanguage == AiBotManager.Language.EN ? "Release to End" : "松开 结束");
                    break;
                case MotionEvent.ACTION_UP:
                    stopIat();
                    mBtnVoice.setText(mBotManager.currentLanguage == AiBotManager.Language.EN ? "Hold to Speak" : "按住 说话");
                    break;
            }
            return true;
        });
    }

    private void initSpeech() {
        mIat = SpeechRecognizer.createRecognizer(this, null);
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
        updateSpeechParams();
    }

    private void updateSpeechParams() {
        if (mBotManager.currentLanguage == AiBotManager.Language.EN) {
            // 设置听写参数 (英文)
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIat.setParameter(SpeechConstant.ACCENT, null);
            
            // 设置合成参数 (英文)
            mTts.setParameter(SpeechConstant.VOICE_NAME, "catherine");
        } else {
            // 设置听写参数 (中文)
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
            
            // 设置合成参数 (中文)
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        }
        
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mTts.setParameter(SpeechConstant.SPEED, "50");
        mTts.setParameter(SpeechConstant.PITCH, "50");
    }

    private void startIat() {
        mIatResults.clear();
        mIat.startListening(mRecognizerListener);
    }

    private void stopIat() {
        mIat.stopListening();
    }

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {}

        @Override
        public void onError(SpeechError error) {
            Toast.makeText(AiChatActivity.this, "Error: " + error.getErrorCode(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            String sn = null;
            try {
                JSONObject resultJson = new JSONObject(results.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIatResults.put(sn, text);

            if (isLast) {
                StringBuilder resultBuffer = new StringBuilder();
                for (String key : mIatResults.keySet()) {
                    resultBuffer.append(mIatResults.get(key));
                }
                String userText = resultBuffer.toString();
                if (!userText.isEmpty()) {
                    handleUserMessage(userText);
                }
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {}

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };

    private void handleUserMessage(String text) {
        addMessage(text, true);
        mBotManager.getResponseAsync(text, new AiBotManager.AiCallback() {
            @Override
            public void onResponse(String aiContent) {
                runOnUiThread(() -> {
                    addMessage(aiContent, false);
                    mTts.startSpeaking(aiContent, null);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    addMessage("Error: " + error, false);
                });
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        mMessages.add(new ChatMessage(text, isUser));
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mMessages.size() - 1);
    }

    private static class ChatMessage {
        String text;
        boolean isUser;
        ChatMessage(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
        }
    }

    private class ChatAdapter extends BaseAdapter {
        @Override
        public int getCount() { return mMessages.size(); }
        @Override
        public Object getItem(int position) { return mMessages.get(position); }
        @Override
        public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(AiChatActivity.this).inflate(R.layout.item_chat, parent, false);
            }
            ChatMessage msg = mMessages.get(position);
            TextView tvBot = convertView.findViewById(R.id.msg_bot);
            TextView tvUser = convertView.findViewById(R.id.msg_user);
            
            if (msg.isUser) {
                tvUser.setVisibility(View.VISIBLE);
                tvBot.setVisibility(View.GONE);
                tvUser.setText(msg.text);
            } else {
                tvBot.setVisibility(View.VISIBLE);
                tvUser.setVisibility(View.GONE);
                tvBot.setText(msg.text);
            }
            return convertView;
        }
    }
}
