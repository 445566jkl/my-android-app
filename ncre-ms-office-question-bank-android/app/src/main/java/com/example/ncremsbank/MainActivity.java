package com.example.ncremsbank;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private JSONArray questions;
    private int index = 0;
    private TextView title;
    private TextView subtitle;
    private TextView questionView;
    private TextView progressView;
    private LinearLayout optionsBox;

    private final int primary = Color.rgb(33, 99, 255);
    private final int primaryDark = Color.rgb(18, 57, 160);
    private final int bg = Color.rgb(244, 247, 255);
    private final int card = Color.WHITE;
    private final int textMain = Color.rgb(24, 33, 55);
    private final int textSub = Color.rgb(104, 116, 140);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        loadQuestions();
        setContentView(buildUi());
        renderQuestion();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(18));
        root.setBackgroundColor(bg);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));
        hero.setBackground(roundGradient(primary, primaryDark, dp(24)));
        root.addView(hero, new LinearLayout.LayoutParams(-1, -2));

        title = new TextView(this);
        title.setText("Office 考证通");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        hero.addView(title);

        subtitle = new TextView(this);
        subtitle.setText("一级 MS Office · 智能刷题 · 错题巩固");
        subtitle.setTextColor(Color.argb(230, 255, 255, 255));
        subtitle.setTextSize(14);
        subtitle.setPadding(0, dp(6), 0, 0);
        hero.addView(subtitle);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setPadding(0, dp(14), 0, 0);
        hero.addView(stats);
        stats.addView(statCard("题库", questions == null ? "0" : String.valueOf(questions.length())));
        stats.addView(statCard("模式", "顺序练习"));
        stats.addView(statCard("目标", "稳过考试"));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(18), dp(18), dp(18));
        panel.setBackground(round(card, dp(22)));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, 0, 1);
        pp.setMargins(0, dp(16), 0, 0);
        root.addView(panel, pp);

        progressView = new TextView(this);
        progressView.setTextColor(primary);
        progressView.setTextSize(14);
        progressView.setTypeface(Typeface.DEFAULT_BOLD);
        panel.addView(progressView);

        questionView = new TextView(this);
        questionView.setTextColor(textMain);
        questionView.setTextSize(19);
        questionView.setLineSpacing(dp(4), 1.0f);
        questionView.setTypeface(Typeface.DEFAULT_BOLD);
        questionView.setPadding(0, dp(12), 0, dp(10));
        panel.addView(questionView);

        optionsBox = new LinearLayout(this);
        optionsBox.setOrientation(LinearLayout.VERTICAL);
        panel.addView(optionsBox, new LinearLayout.LayoutParams(-1, 0, 1));

        Button next = new Button(this);
        next.setText("下一题");
        next.setTextColor(Color.WHITE);
        next.setTextSize(16);
        next.setBackground(round(primary, dp(16)));
        next.setOnClickListener(v -> {
            if (questions != null && questions.length() > 0) {
                index = (index + 1) % questions.length();
                renderQuestion();
            }
        });
        panel.addView(next, new LinearLayout.LayoutParams(-1, dp(50)));

        TextView foot = new TextView(this);
        foot.setText("原创学习工具 · 本应用不使用第三方教育品牌名称与素材");
        foot.setGravity(Gravity.CENTER);
        foot.setTextColor(textSub);
        foot.setTextSize(12);
        foot.setPadding(0, dp(10), 0, 0);
        root.addView(foot);
        return root;
    }

    private TextView statCard(String label, String value) {
        TextView v = new TextView(this);
        v.setText(label + "\n" + value);
        v.setTextColor(Color.WHITE);
        v.setTextSize(13);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(8), dp(9), dp(8), dp(9));
        v.setBackground(round(Color.argb(46, 255, 255, 255), dp(16)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        lp.setMargins(0, 0, dp(8), 0);
        v.setLayoutParams(lp);
        return v;
    }

    private void renderQuestion() {
        optionsBox.removeAllViews();
        if (questions == null || questions.length() == 0) {
            progressView.setText("题库未加载");
            questionView.setText("请检查 assets/questions.json 是否存在。");
            return;
        }
        try {
            JSONObject q = questions.getJSONObject(index);
            progressView.setText("第 " + (index + 1) + " / " + questions.length() + " 题");
            questionView.setText(q.optString("question", ""));
            JSONArray opts = q.optJSONArray("options");
            String answer = q.optString("answer", "");
            if (opts != null) {
                for (int i = 0; i < opts.length(); i++) {
                    String opt = opts.getString(i);
                    TextView item = new TextView(this);
                    item.setText(opt);
                    item.setTextSize(16);
                    item.setTextColor(textMain);
                    item.setPadding(dp(14), dp(13), dp(14), dp(13));
                    item.setBackground(roundStroke(Color.rgb(250, 252, 255), Color.rgb(222, 229, 246), dp(16)));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                    lp.setMargins(0, dp(7), 0, dp(7));
                    optionsBox.addView(item, lp);
                    item.setOnClickListener(v -> {
                        boolean ok = opt.startsWith(answer) || opt.startsWith(answer + ".") || opt.startsWith(answer + "、");
                        item.setTextColor(ok ? Color.rgb(15, 132, 82) : Color.rgb(210, 64, 64));
                        item.setBackground(roundStroke(ok ? Color.rgb(232, 250, 241) : Color.rgb(255, 238, 238), ok ? Color.rgb(73, 190, 129) : Color.rgb(228, 96, 96), dp(16)));
                        Toast.makeText(this, ok ? "答对了，继续保持！" : "先收藏思路，再看解析巩固。正确答案：" + answer, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        } catch (Exception e) {
            questionView.setText("题目显示失败：" + e.getMessage());
        }
    }

    private void loadQuestions() {
        try {
            InputStream is = getAssets().open("questions.json");
            byte[] data = new byte[is.available()];
            is.read(data);
            is.close();
            questions = new JSONArray(new String(data, StandardCharsets.UTF_8));
        } catch (Exception e) {
            questions = new JSONArray();
        }
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        return g;
    }

    private GradientDrawable roundStroke(int color, int stroke, int radius) {
        GradientDrawable g = round(color, radius);
        g.setStroke(dp(1), stroke);
        return g;
    }

    private GradientDrawable roundGradient(int start, int end, int radius) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        g.setCornerRadius(radius);
        return g;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
