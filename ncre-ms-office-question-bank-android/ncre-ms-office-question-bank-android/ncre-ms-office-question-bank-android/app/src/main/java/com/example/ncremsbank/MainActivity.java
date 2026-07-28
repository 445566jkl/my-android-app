package com.example.ncremsbank;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class MainActivity extends Activity {
    private JSONArray allQuestions = new JSONArray();
    private ArrayList<JSONObject> paper = new ArrayList<>();
    private int index = 0;
    private String mode = "home";
    private String[] userAnswers;
    private HashSet<Integer> wrongIds = new HashSet<>();
    private SharedPreferences sp;

    private LinearLayout root;
    private final int blue = Color.rgb(39, 104, 255);
    private final int dark = Color.rgb(28, 40, 72);
    private final int bg = Color.rgb(245, 247, 252);
    private final int green = Color.rgb(24, 155, 93);
    private final int red = Color.rgb(220, 72, 72);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("ncre_ms_practice", MODE_PRIVATE);
        loadQuestions();
        loadWrongIds();
        showHome();
    }

    private void base() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(18));
        root.setBackgroundColor(bg);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void showHome() {
        mode = "home";
        base();
        TextView title = title("全国计算机等级考试\n一级MS刷题助手", 25, Color.WHITE);
        LinearLayout hero = card(blue, 22, dp(18));
        hero.addView(title);
        TextView sub = text("考试模式 + 背题模式 + 错题复习", 15, Color.WHITE);
        sub.setPadding(0, dp(8), 0, 0);
        hero.addView(sub);
        root.addView(hero, mp(-1, -2, 0, 0, 0, dp(16)));

        root.addView(menuButton("考试模式：随机抽 20 题，提交后评分", v -> startExam()));
        root.addView(menuButton("背题模式：直接显示答案和解析", v -> startStudy(false)));
        root.addView(menuButton("错题记录：查看并复习做错题", v -> showWrongList()));

        TextView info = text("题库总数：" + allQuestions.length() + " 题\n错题记录：" + wrongIds.size() + " 题", 15, dark);
        info.setPadding(dp(6), dp(12), dp(6), 0);
        root.addView(info);
    }

    private void startExam() {
        mode = "exam";
        paper = randomQuestions(20);
        userAnswers = new String[paper.size()];
        index = 0;
        renderQuestion(false, false);
    }

    private void startStudy(boolean onlyWrong) {
        mode = onlyWrong ? "wrongStudy" : "study";
        paper.clear();
        for (int i = 0; i < allQuestions.length(); i++) {
            JSONObject q = allQuestions.optJSONObject(i);
            if (q == null) continue;
            if (!onlyWrong || wrongIds.contains(q.optInt("id"))) paper.add(q);
        }
        if (paper.isEmpty()) {
            Toast.makeText(this, onlyWrong ? "还没有错题记录" : "题库为空", Toast.LENGTH_SHORT).show();
            showHome();
            return;
        }
        index = 0;
        renderQuestion(true, onlyWrong);
    }

    private void renderQuestion(boolean revealAnswer, boolean fromWrong) {
        base();
        JSONObject q = paper.get(index);
        String answer = q.optString("answer", "");

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        Button back = smallButton("首页");
        back.setOnClickListener(v -> showHome());
        top.addView(back, mp(0, dp(42), 1, 0, dp(8), 0));
        TextView progress = text((index + 1) + " / " + paper.size(), 16, blue);
        progress.setGravity(Gravity.CENTER);
        progress.setTypeface(Typeface.DEFAULT_BOLD);
        top.addView(progress, mp(0, dp(42), 1, 0, 0, 0));
        root.addView(top);

        TextView modeText = text(mode.equals("exam") ? "考试模式：选择后不提示对错，全部做完再提交" : "背题模式：答案已显示，适合快速记忆", 14, Color.rgb(95, 108, 132));
        modeText.setPadding(0, dp(10), 0, dp(10));
        root.addView(modeText);

        LinearLayout box = card(Color.WHITE, 18, dp(16));
        TextView question = title("第" + (index + 1) + "题  " + q.optString("question", ""), 19, dark);
        box.addView(question);

        JSONObject opts = q.optJSONObject("options");
        String chosen = userAnswers == null ? "" : userAnswers[index];
        String[] letters = new String[]{"A", "B", "C", "D"};
        for (String key : letters) {
            if (opts == null || !opts.has(key)) continue;
            String optText = key + ". " + opts.optString(key);
            TextView item = optionView(optText);
            if (mode.equals("exam")) {
                if (key.equals(chosen)) {
                    item.setBackground(roundStroke(Color.rgb(232, 239, 255), blue, 14));
                    item.setTextColor(blue);
                }
                item.setOnClickListener(v -> {
                    userAnswers[index] = key;
                    renderQuestion(false, false);
                });
            } else {
                if (key.equals(answer)) {
                    item.setBackground(roundStroke(Color.rgb(232, 250, 241), green, 14));
                    item.setTextColor(green);
                }
            }
            box.addView(item, mp(-1, -2, 0, dp(8), 0, 0));
        }

        if (revealAnswer) {
            TextView ans = text("正确答案：" + answer, 17, green);
            ans.setTypeface(Typeface.DEFAULT_BOLD);
            ans.setPadding(0, dp(12), 0, dp(4));
            box.addView(ans);
            TextView exp = text(explainText(q), 14, Color.rgb(92, 102, 122));
            box.addView(exp);
        }
        root.addView(box, mp(-1, -2, 0, 0, 0, dp(14)));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        Button prev = button("上一题");
        prev.setEnabled(index > 0);
        prev.setOnClickListener(v -> { if (index > 0) { index--; renderQuestion(revealAnswer, fromWrong); } });
        nav.addView(prev, mp(0, dp(50), 1, 0, dp(8), 0));

        if (mode.equals("exam") && index == paper.size() - 1) {
            Button submit = button("提交评分");
            submit.setOnClickListener(v -> submitExam());
            nav.addView(submit, mp(0, dp(50), 1, 0, 0, 0));
        } else {
            Button next = button("下一题");
            next.setEnabled(index < paper.size() - 1);
            next.setOnClickListener(v -> { if (index < paper.size() - 1) { index++; renderQuestion(revealAnswer, fromWrong); } });
            nav.addView(next, mp(0, dp(50), 1, 0, 0, 0));
        }
        root.addView(nav);
    }

    private void submitExam() {
        int score = 0;
        ArrayList<JSONObject> wrongNow = new ArrayList<>();
        for (int i = 0; i < paper.size(); i++) {
            JSONObject q = paper.get(i);
            boolean ok = q.optString("answer", "").equals(userAnswers[i]);
            if (ok) score++;
            else {
                wrongNow.add(q);
                wrongIds.add(q.optInt("id"));
            }
        }
        saveWrongIds();
        showResult(score, wrongNow);
    }

    private void showResult(int score, ArrayList<JSONObject> wrongNow) {
        base();
        LinearLayout result = card(Color.WHITE, 20, dp(18));
        TextView t = title("本次得分：" + score + " / 20", 26, score >= 16 ? green : red);
        result.addView(t);
        TextView tip = text("每题 1 分。下面会列出本次做错的题，已自动加入错题记录。", 15, Color.rgb(90, 102, 126));
        tip.setPadding(0, dp(8), 0, dp(8));
        result.addView(tip);
        root.addView(result, mp(-1, -2, 0, 0, 0, dp(14)));

        if (wrongNow.isEmpty()) {
            root.addView(text("恭喜，本次 20 题全部答对！", 18, green));
        } else {
            root.addView(title("本次错题", 20, dark));
            for (JSONObject q : wrongNow) {
                root.addView(resultWrongCard(q));
            }
        }
        root.addView(menuButton("回到首页", v -> showHome()));
        root.addView(menuButton("进入错题记录复习", v -> startStudy(true)));
    }

    private View resultWrongCard(JSONObject q) {
        LinearLayout c = card(Color.WHITE, 16, dp(14));
        c.addView(title(q.optString("question", ""), 16, dark));
        String answer = q.optString("answer", "");
        JSONObject opts = q.optJSONObject("options");
        String ansText = answer;
        if (opts != null) ansText += ". " + opts.optString(answer, "");
        TextView a = text("正确答案：" + ansText, 15, green);
        a.setPadding(0, dp(8), 0, 0);
        c.addView(a);
        return c;
    }

    private void showWrongList() {
        if (wrongIds.isEmpty()) {
            Toast.makeText(this, "还没有错题，先去考试模式做一套题吧", Toast.LENGTH_SHORT).show();
            showHome();
            return;
        }
        startStudy(true);
    }

    private ArrayList<JSONObject> randomQuestions(int count) {
        ArrayList<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < allQuestions.length(); i++) {
            JSONObject q = allQuestions.optJSONObject(i);
            if (q != null) list.add(q);
        }
        Collections.shuffle(list);
        ArrayList<JSONObject> picked = new ArrayList<>();
        for (int i = 0; i < Math.min(count, list.size()); i++) picked.add(list.get(i));
        return picked;
    }

    private void loadQuestions() {
        try {
            InputStream is = getAssets().open("questions.json");
            byte[] data = new byte[is.available()];
            is.read(data);
            is.close();
            allQuestions = new JSONArray(new String(data, StandardCharsets.UTF_8));
        } catch (Exception e) {
            allQuestions = new JSONArray();
        }
    }

    private void loadWrongIds() {
        wrongIds.clear();
        String s = sp.getString("wrong_ids", "");
        if (s == null || s.length() == 0) return;
        String[] arr = s.split(",");
        for (String x : arr) {
            try { wrongIds.add(Integer.parseInt(x)); } catch (Exception ignored) {}
        }
    }

    private void saveWrongIds() {
        StringBuilder sb = new StringBuilder();
        for (Integer id : wrongIds) {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        sp.edit().putString("wrong_ids", sb.toString()).apply();
    }

    private String explainText(JSONObject q) {
        Object e = q.opt("explain");
        if (e == null) return "解析：暂无。";
        if (e instanceof JSONArray) {
            JSONArray arr = (JSONArray) e;
            if (arr.length() == 0) return "解析：暂无。";
            StringBuilder sb = new StringBuilder("解析：");
            for (int i = 0; i < arr.length(); i++) sb.append(arr.optString(i));
            return sb.toString();
        }
        String s = String.valueOf(e);
        return s.length() == 0 ? "解析：暂无。" : "解析：" + s;
    }

    private TextView title(String s, int size, int color) {
        TextView v = text(s, size, color);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    private TextView text(String s, int size, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setLineSpacing(dp(3), 1.0f);
        return v;
    }

    private TextView optionView(String s) {
        TextView v = text(s, 16, dark);
        v.setPadding(dp(14), dp(13), dp(14), dp(13));
        v.setBackground(roundStroke(Color.rgb(250, 252, 255), Color.rgb(220, 228, 244), 14));
        return v;
    }

    private Button menuButton(String s, View.OnClickListener l) {
        Button b = button(s);
        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setPadding(dp(16), 0, dp(16), 0);
        b.setOnClickListener(l);
        b.setAllCaps(false);
        LinearLayout.LayoutParams lp = mp(-1, dp(56), 0, 0, 0, dp(10));
        b.setLayoutParams(lp);
        return b;
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(15);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(blue, 14));
        b.setAllCaps(false);
        return b;
    }

    private Button smallButton(String s) {
        Button b = button(s);
        b.setTextSize(14);
        return b;
    }

    private LinearLayout card(int color, int radius, int padding) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(padding, padding, padding, padding);
        l.setBackground(round(color, radius));
        return l;
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private GradientDrawable roundStroke(int color, int strokeColor, int radiusDp) {
        GradientDrawable g = round(color, radiusDp);
        g.setStroke(dp(1), strokeColor);
        return g;
    }

    private LinearLayout.LayoutParams mp(int w, int h, int weight, int l, int t, int r) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h, weight);
        p.setMargins(l, t, r, 0);
        return p;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
