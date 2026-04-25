package com.iflytek.voicedemo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import android.widget.ProgressBar;
import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.ise.result.ReadSentenceResult;
import com.iflytek.ise.result.Result;
import com.iflytek.ise.result.xml.XmlResultParser;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class EvaluationActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "EvaluationActivity";

    private TextView tvPracticeText;
    private TextView tvResult;
    private ImageView btnRecord;
    private View btnBack;
    private Button btnSwitchLanguage;
    private EditText etCustomSentence;
    private Button btnSetSentence;
    private TextView tvEvalEmoji;
    private TextView tvEvalStatus;
    private ProgressBar pbScoreCircle;
    private TextView tvScoreSmall;
    private View layoutScoreClick;
    private View layoutEvalHeader;

    private SpeechEvaluator mIse;
    private String mPracticeText = "The quick brown fox jumps over the lazy dog.";
    private String mLastResult;
    private String mCurrentLanguage = "en_us"; // 默认英文
    private float mTotalScore = 0;
    
    // 模拟的各项分数
    private float mAccuracyScore = 0;
    private float mFluencyScore = 0;
    private float mProsodyScore = 0;
    private float mPronunciationScore = 0;
    private float mIntegrityScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_evaluation);

        // 确保 MSC 初始化
        SpeechApp.initializeMsc(this);
        mIse = SpeechEvaluator.createEvaluator(this, null);

        initUI();
    }

    private void initUI() {
        tvPracticeText = findViewById(R.id.tv_practice_text);
        tvResult = findViewById(R.id.tv_result);
        btnRecord = findViewById(R.id.btn_record);
        btnBack = findViewById(R.id.btn_back);
        btnSwitchLanguage = findViewById(R.id.btn_switch_language);
        etCustomSentence = findViewById(R.id.et_custom_sentence);
        btnSetSentence = findViewById(R.id.btn_set_sentence);
        
        tvEvalEmoji = findViewById(R.id.tv_eval_emoji);
        tvEvalStatus = findViewById(R.id.tv_eval_status);
        pbScoreCircle = findViewById(R.id.pb_score_circle);
        tvScoreSmall = findViewById(R.id.tv_score_small);
        layoutScoreClick = findViewById(R.id.layout_score_click);
        layoutEvalHeader = findViewById(R.id.layout_eval_header);

        tvPracticeText.setText(mPracticeText);

        btnRecord.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnSwitchLanguage.setOnClickListener(this);
        btnSetSentence.setOnClickListener(this);
        layoutScoreClick.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            finish();
        } else if (id == R.id.btn_record) {
            startEvaluation();
        } else if (id == R.id.btn_switch_language) {
            switchLanguage();
        } else if (id == R.id.btn_set_sentence) {
            setCustomSentence();
        } else if (id == R.id.layout_score_click) {
            showScoreDetailDialog();
        }
    }

    private void showScoreDetailDialog() {
        if (mTotalScore <= 0) {
            showTip("请先完成一次评测");
            return;
        }
        
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_score_details, null);
        dialog.setContentView(view);
        
        RadarChartView radarChart = view.findViewById(R.id.radar_chart);
        TextView tvDetailAccuracy = view.findViewById(R.id.tv_detail_accuracy);
        TextView tvDetailProsody = view.findViewById(R.id.tv_detail_prosody);
        TextView tvDetailFluency = view.findViewById(R.id.tv_detail_fluency);
        ImageView btnClose = view.findViewById(R.id.btn_close_dialog);

        radarChart.setData(new float[]{mAccuracyScore, mFluencyScore, mPronunciationScore, mProsodyScore, mIntegrityScore});
        tvDetailAccuracy.setText(String.format("%.0f分", mAccuracyScore));
        tvDetailProsody.setText(String.format("%.0f分", mProsodyScore));
        tvDetailFluency.setText(String.format("%.0f分", mFluencyScore));
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void switchLanguage() {
        if ("en_us".equals(mCurrentLanguage)) {
            mCurrentLanguage = "zh_cn";
            btnSwitchLanguage.setText("CN");
            mPracticeText = "今天天气真不错。"; // 默认中文句子
            showTip("已切换到中文评测");
        } else {
            mCurrentLanguage = "en_us";
            btnSwitchLanguage.setText("EN");
            mPracticeText = "The quick brown fox jumps over the lazy dog."; // 默认英文句子
            showTip("已切换到英文评测");
        }
        tvPracticeText.setText(mPracticeText);
        etCustomSentence.setText("");
    }

    private void setCustomSentence() {
        String text = etCustomSentence.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            showTip("请输入要练习的句子");
            return;
        }
        mPracticeText = text;
        tvPracticeText.setText(mPracticeText);
        showTip("练习句子已更新");
    }

    private void startEvaluation() {
        if (mIse == null) {
            showTip("评测对象未初始化");
            return;
        }

        if (mIse.isEvaluating()) {
            mIse.stopEvaluating();
            btnRecord.setImageResource(android.R.drawable.ic_btn_speak_now);
            return;
        }

        tvResult.setText("正在录音...");
        setParams();
        int ret = mIse.startEvaluating(mPracticeText, null, mEvaluatorListener);
        if (ret == 0) {
            btnRecord.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            showTip("启动评测失败，错误码：" + ret);
        }
    }

    private void setParams() {
        mIse.setParameter(SpeechConstant.LANGUAGE, mCurrentLanguage);
        if ("zh_cn".equals(mCurrentLanguage)) {
            mIse.setParameter(SpeechConstant.ISE_CATEGORY, "read_sentence");
            mIse.setParameter("ent", "cn_vip");
        } else {
            mIse.setParameter(SpeechConstant.ISE_CATEGORY, "read_sentence");
            mIse.setParameter("ent", "en_vip");
        }
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mIse.setParameter(SpeechConstant.VAD_BOS, "5000");
        mIse.setParameter(SpeechConstant.VAD_EOS, "1800");
        mIse.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");
        mIse.setParameter(SpeechConstant.RESULT_LEVEL, "complete");
        mIse.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIse.setParameter(SpeechConstant.ISE_AUDIO_PATH, getExternalFilesDir("msc").getAbsolutePath() + "/ise.wav");
        
        // 云端评分所需参数
        mIse.setParameter(SpeechConstant.SUBJECT, "ise");
        mIse.setParameter("plev", "0");
        mIse.setParameter("ise_unite", "1");
        mIse.setParameter("rst", "entirety");
    }

    private EvaluatorListener mEvaluatorListener = new EvaluatorListener() {
        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG, "onBeginOfSpeech");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech");
            runOnUiThread(() -> {
                tvResult.setText("正在评分...");
                btnRecord.setImageResource(android.R.drawable.ic_btn_speak_now);
            });
        }

        @Override
        public void onResult(EvaluatorResult result, boolean isLast) {
            if (isLast) {
                mLastResult = result.getResultString();
                runOnUiThread(() -> {
                    parseResult(mLastResult);
                });
            }
        }

        @Override
        public void onError(SpeechError error) {
            runOnUiThread(() -> {
                btnRecord.setImageResource(android.R.drawable.ic_btn_speak_now);
                if (error != null) {
                    showTip("错误: " + error.getErrorCode() + " " + error.getErrorDescription());
                    tvResult.setText("评测出错");
                }
            });
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {}

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };

    private void parseResult(String xml) {
        try {
            XmlResultParser resultParser = new XmlResultParser();
            Result result = resultParser.parse(xml);
            if (result != null) {
                mTotalScore = result.total_score;
                
                // 模拟详细分数逻辑 (实际开发中应从 XML 进一步解析)
                // 确保分数不小于 0
                mAccuracyScore = Math.max(0, Math.min(100, mTotalScore + (float)(Math.random() * 10 - 5)));
                mFluencyScore = Math.max(0, Math.min(100, mTotalScore + (float)(Math.random() * 10 - 5)));
                mProsodyScore = Math.max(0, Math.min(100, mTotalScore + (float)(Math.random() * 10 - 5)));
                mPronunciationScore = Math.max(0, Math.min(100, mTotalScore + (float)(Math.random() * 10 - 5)));
                mIntegrityScore = Math.max(0, Math.min(100, mTotalScore + (float)(Math.random() * 10 - 5)));

                updateScoreUI();
                
                String feedback = String.format("总分: %.1f\n", mTotalScore);
                if (result instanceof ReadSentenceResult) {
                    feedback += "\n" + result.toString();
                }
                tvResult.setText(feedback);
            } else {
                tvResult.setText("解析结果失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "parseResult error", e);
            tvResult.setText("结果解析出错");
        }
    }

    private void updateScoreUI() {
        tvScoreSmall.setText(String.format("%.0f分", mTotalScore));
        pbScoreCircle.setProgress((int) mTotalScore);
        
        // 动态设置进度条颜色
        int color;
        if (mTotalScore >= 90) {
            tvEvalEmoji.setText("🥳");
            tvEvalStatus.setText("太棒啦");
            color = Color.parseColor("#10B981");
            tvEvalStatus.setTextColor(color);
        } else if (mTotalScore >= 70) {
            tvEvalEmoji.setText("😀");
            tvEvalStatus.setText("不错哦");
            color = Color.parseColor("#F59E0B");
            tvEvalStatus.setTextColor(color);
        } else {
            tvEvalEmoji.setText("😢");
            tvEvalStatus.setText("再接再厉");
            color = Color.parseColor("#EF4444");
            tvEvalStatus.setTextColor(color);
        }
        
        // 更新圆环颜色
        android.graphics.drawable.LayerDrawable drawable = (android.graphics.drawable.LayerDrawable) pbScoreCircle.getProgressDrawable();
        android.graphics.drawable.RotateDrawable rotateDrawable = (android.graphics.drawable.RotateDrawable) drawable.findDrawableByLayerId(android.R.id.progress);
        android.graphics.drawable.GradientDrawable ringDrawable = (android.graphics.drawable.GradientDrawable) rotateDrawable.getDrawable();
        if (ringDrawable != null) {
            ringDrawable.setColor(color);
        }
    }

    private void showTip(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIse != null) {
            mIse.destroy();
            mIse = null;
        }
    }
}
