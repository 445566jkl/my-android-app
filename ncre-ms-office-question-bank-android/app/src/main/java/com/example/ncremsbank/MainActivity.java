package com.example.ncremsbank;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity {
    private final List<Question> questions = new ArrayList<>();
    private final List<Integer> examOrder = new ArrayList<>();
    private int currentIndex = 0;
    private int examPos = 0;
    private TextView titleView, questionView, progressView, answerView, statsView;
    private LinearLayout optionBox;
    private EditText jumpInput, searchInput;
    private Button showButton, favButton, modeButton, examButton;
    private boolean answerVisible = false;
    private String mode = "all";
    private SharedPreferences sp;
    private final Set<String> wrongIds = new HashSet<>();
    private final Set<String> favoriteIds = new HashSet<>();
    private final Set<String> doneIds = new HashSet<>();

    static class Question { int id; String question, a, b, c, d, answer, explain; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("study", MODE_PRIVATE);
        loadQuestions(); loadState(); buildUi(); renderQuestion();
    }

    private void loadQuestions() {
        try {
            InputStream in = getAssets().open("questions.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; int len;
            while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
            JSONArray arr = new JSONArray(new String(out.toByteArray(), StandardCharsets.UTF_8));
            for (int i=0;i<arr.length();i++) {
                JSONObject obj = arr.getJSONObject(i); JSONObject opts = obj.getJSONObject("options");
                Question q = new Question();
                q.id=obj.getInt("id"); q.question=obj.getString("question");
                q.a=opts.optString("A",""); q.b=opts.optString("B",""); q.c=opts.optString("C",""); q.d=opts.optString("D","");
                q.answer=obj.getString("answer");
                JSONArray exp=obj.optJSONArray("explain"); StringBuilder sb=new StringBuilder();
                if(exp!=null){ for(int j=0;j<exp.length();j++){ if(j>0) sb.append("\n"); sb.append(exp.getString(j)); } }
                q.explain=sb.toString(); questions.add(q);
            }
        } catch(Exception e){ Toast.makeText(this,"题库加载失败："+e.getMessage(),Toast.LENGTH_LONG).show(); }
    }

    private void loadState(){
        currentIndex=sp.getInt("currentIndex",0);
        wrongIds.addAll(sp.getStringSet("wrongIds",new HashSet<>()));
        favoriteIds.addAll(sp.getStringSet("favoriteIds",new HashSet<>()));
        doneIds.addAll(sp.getStringSet("doneIds",new HashSet<>()));
        if(currentIndex<0 || currentIndex>=Math.max(questions.size(),1)) currentIndex=0;
    }
    private void saveState(){ sp.edit().putInt("currentIndex",currentIndex).putStringSet("wrongIds",new HashSet<>(wrongIds)).putStringSet("favoriteIds",new HashSet<>(favoriteIds)).putStringSet("doneIds",new HashSet<>(doneIds)).apply(); }

    private void buildUi(){
        ScrollView scroll=new ScrollView(this); LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(18),dp(16),dp(18)); root.setBackgroundColor(Color.rgb(246,248,252)); scroll.addView(root);
        titleView=new TextView(this); titleView.setText("一级 MS Office 选择题"); titleView.setTextSize(22); titleView.setTypeface(Typeface.DEFAULT_BOLD); titleView.setTextColor(Color.rgb(25,43,75)); root.addView(titleView);
        progressView=new TextView(this); progressView.setTextSize(14); progressView.setTextColor(Color.rgb(92,104,122)); progressView.setPadding(0,dp(8),0,dp(4)); root.addView(progressView);
        statsView=new TextView(this); statsView.setTextSize(13); statsView.setTextColor(Color.rgb(116,126,143)); statsView.setPadding(0,0,0,dp(12)); root.addView(statsView);
        questionView=new TextView(this); questionView.setTextSize(18); questionView.setTextColor(Color.rgb(28,31,36)); questionView.setLineSpacing(dp(3),1.0f); questionView.setTypeface(Typeface.DEFAULT_BOLD); root.addView(questionView);
        optionBox=new LinearLayout(this); optionBox.setOrientation(LinearLayout.VERTICAL); optionBox.setPadding(0,dp(12),0,dp(8)); root.addView(optionBox);
        answerView=new TextView(this); answerView.setTextSize(16); answerView.setTextColor(Color.rgb(16,115,68)); answerView.setBackgroundColor(Color.rgb(232,247,239)); answerView.setPadding(dp(12),dp(10),dp(12),dp(10)); answerView.setVisibility(View.GONE); root.addView(answerView);

        LinearLayout row1=row(); Button prev=makeButton("上一题"); showButton=makeButton("显示答案"); Button next=makeButton("下一题"); row1.addView(prev,lp()); row1.addView(showButton,lp()); row1.addView(next,lp()); root.addView(row1);
        LinearLayout row2=row(); favButton=makeButton("收藏"); modeButton=makeButton("全部题"); Button random=makeButton("随机题"); row2.addView(favButton,lp()); row2.addView(modeButton,lp()); row2.addView(random,lp()); root.addView(row2);
        LinearLayout row3=row(); examButton=makeButton("模拟考试"); Button clearWrong=makeButton("清错题"); Button reset=makeButton("重置进度"); row3.addView(examButton,lp()); row3.addView(clearWrong,lp()); row3.addView(reset,lp()); root.addView(row3);
        prev.setOnClickListener(v->move(-1)); next.setOnClickListener(v->move(1)); showButton.setOnClickListener(v->toggleAnswer()); favButton.setOnClickListener(v->toggleFavorite()); modeButton.setOnClickListener(v->switchMode()); random.setOnClickListener(v->randomQuestion()); examButton.setOnClickListener(v->toggleExam());
        clearWrong.setOnClickListener(v->{ wrongIds.clear(); if("wrong".equals(mode)) mode="all"; saveState(); toast("错题已清空"); renderQuestion(); });
        reset.setOnClickListener(v->{ doneIds.clear(); wrongIds.clear(); favoriteIds.clear(); mode="all"; examOrder.clear(); examPos=0; saveState(); toast("进度、错题、收藏已重置"); renderQuestion(); });

        LinearLayout searchBox=new LinearLayout(this); searchBox.setOrientation(LinearLayout.HORIZONTAL); searchBox.setGravity(Gravity.CENTER_VERTICAL); searchBox.setPadding(0,dp(10),0,0); root.addView(searchBox);
        searchInput=new EditText(this); searchInput.setHint("搜索关键词，如：Excel、二进制"); searchInput.setTextSize(15); searchBox.addView(searchInput,new LinearLayout.LayoutParams(0,dp(48),1));
        Button search=makeButton("搜索"); searchBox.addView(search,new LinearLayout.LayoutParams(dp(92),dp(48))); search.setOnClickListener(v->searchQuestion());

        LinearLayout jumpBox=new LinearLayout(this); jumpBox.setOrientation(LinearLayout.HORIZONTAL); jumpBox.setGravity(Gravity.CENTER_VERTICAL); jumpBox.setPadding(0,dp(8),0,0); root.addView(jumpBox);
        jumpInput=new EditText(this); jumpInput.setHint("输入题号 1-400"); jumpInput.setInputType(InputType.TYPE_CLASS_NUMBER); jumpInput.setTextSize(15); jumpBox.addView(jumpInput,new LinearLayout.LayoutParams(0,dp(48),1));
        Button jump=makeButton("跳转"); jumpBox.addView(jump,new LinearLayout.LayoutParams(dp(92),dp(48))); jump.setOnClickListener(v->jumpToQuestion());
        TextView note=new TextView(this); note.setText("功能：离线刷题、搜索、随机练习、模拟考试、错题本、收藏、自动保存进度。题库共 400 道。"); note.setTextSize(13); note.setTextColor(Color.rgb(116,126,143)); note.setPadding(0,dp(16),0,0); root.addView(note);
        setContentView(scroll);
    }

    private LinearLayout row(){ LinearLayout r=new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); r.setGravity(Gravity.CENTER); r.setPadding(0,dp(6),0,0); return r; }
    private LinearLayout.LayoutParams lp(){ return new LinearLayout.LayoutParams(0,dp(46),1); }
    private Button makeButton(String text){ Button b=new Button(this); b.setText(text); b.setTextSize(13); b.setAllCaps(false); return b; }
    private TextView makeOption(String label,String value){ TextView tv=new TextView(this); tv.setText(label+"："+value); tv.setTextSize(16); tv.setTextColor(Color.rgb(38,45,57)); tv.setPadding(dp(12),dp(10),dp(12),dp(10)); LinearLayout.LayoutParams m=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT); m.setMargins(0,0,0,dp(8)); tv.setLayoutParams(m); tv.setBackgroundColor(Color.WHITE); tv.setOnClickListener(v->choose(label)); return tv; }

    private void renderQuestion(){
        if(questions.isEmpty()){ questionView.setText("题库为空，请检查 assets/questions.json"); return; }
        if(!isCurrentVisible()){ int idx=findNextVisible(currentIndex,1); if(idx>=0) currentIndex=idx; else { mode="all"; currentIndex=0; } }
        Question q=questions.get(currentIndex); answerVisible=false;
        String exam = "exam".equals(mode) ? "｜考试第 "+(examPos+1)+"/50" : "";
        progressView.setText("第 "+q.id+" 题 / 共 "+questions.size()+" 题"+exam);
        statsView.setText("已做 "+doneIds.size()+" 道｜错题 "+wrongIds.size()+" 道｜收藏 "+favoriteIds.size()+" 道｜模式："+modeName());
        questionView.setText(q.id+"、"+q.question); optionBox.removeAllViews(); optionBox.addView(makeOption("A",q.a)); optionBox.addView(makeOption("B",q.b)); optionBox.addView(makeOption("C",q.c)); optionBox.addView(makeOption("D",q.d));
        String ans="正确答案："+q.answer; if(q.explain!=null && q.explain.trim().length()>0) ans += "\n解析："+q.explain.trim(); answerView.setText(ans); answerView.setVisibility(View.GONE); showButton.setText("显示答案"); favButton.setText(favoriteIds.contains(String.valueOf(q.id))?"已收藏":"收藏"); modeButton.setText(modeName()); examButton.setText("exam".equals(mode)?"退出考试":"模拟考试"); saveState();
    }

    private void choose(String choice){ Question q=questions.get(currentIndex); doneIds.add(String.valueOf(q.id)); answerVisible=true; answerView.setVisibility(View.VISIBLE); showButton.setText("隐藏答案"); if(choice.equals(q.answer)){ wrongIds.remove(String.valueOf(q.id)); toast("答对了"); } else { wrongIds.add(String.valueOf(q.id)); toast("答错了，已加入错题"); } saveState(); if("exam".equals(mode) && examPos < examOrder.size()-1){ examPos++; currentIndex=examOrder.get(examPos); } renderQuestion(); }
    private void toggleAnswer(){ answerVisible=!answerVisible; answerView.setVisibility(answerVisible?View.VISIBLE:View.GONE); showButton.setText(answerVisible?"隐藏答案":"显示答案"); }
    private void toggleFavorite(){ Question q=questions.get(currentIndex); String id=String.valueOf(q.id); if(favoriteIds.contains(id)){ favoriteIds.remove(id); toast("已取消收藏"); } else { favoriteIds.add(id); toast("已收藏"); } saveState(); renderQuestion(); }
    private void switchMode(){ if("all".equals(mode)){ if(wrongIds.isEmpty()) toast("暂无错题"); else mode="wrong"; } else if("wrong".equals(mode)){ if(favoriteIds.isEmpty()) toast("暂无收藏"); else mode="favorite"; } else { mode="all"; examOrder.clear(); examPos=0; } renderQuestion(); }
    private void randomQuestion(){ if(questions.isEmpty()) return; mode="all"; currentIndex=new Random().nextInt(questions.size()); renderQuestion(); }
    private void toggleExam(){ if("exam".equals(mode)){ mode="all"; examOrder.clear(); examPos=0; toast("已退出模拟考试"); } else { startExam(); } renderQuestion(); }
    private void startExam(){ mode="exam"; examOrder.clear(); ArrayList<Integer> ids=new ArrayList<>(); for(int i=0;i<questions.size();i++) ids.add(i); Collections.shuffle(ids); int count=Math.min(50,ids.size()); for(int i=0;i<count;i++) examOrder.add(ids.get(i)); examPos=0; currentIndex=examOrder.get(0); toast("已生成 50 题模拟考试"); }
    private boolean isVisibleIndex(int idx){ if(idx<0 || idx>=questions.size()) return false; String id=String.valueOf(questions.get(idx).id); if("wrong".equals(mode)) return wrongIds.contains(id); if("favorite".equals(mode)) return favoriteIds.contains(id); if("exam".equals(mode)) return examOrder.contains(idx); return true; }
    private boolean isCurrentVisible(){ return isVisibleIndex(currentIndex); }
    private int findNextVisible(int start,int step){ if(questions.isEmpty()) return -1; if("exam".equals(mode) && !examOrder.isEmpty()){ examPos += step; if(examPos<0) examPos=examOrder.size()-1; if(examPos>=examOrder.size()) examPos=0; return examOrder.get(examPos); } int i=start; for(int c=0;c<questions.size();c++){ i+=step; if(i<0) i=questions.size()-1; if(i>=questions.size()) i=0; if(isVisibleIndex(i)) return i; } return -1; }
    private void move(int step){ int idx=findNextVisible(currentIndex,step); if(idx>=0){ currentIndex=idx; renderQuestion(); } }
    private void searchQuestion(){ String key=searchInput.getText().toString().trim().toLowerCase(); if(key.length()==0){ toast("请输入搜索关键词"); return; } for(int i=currentIndex+1;i<questions.size();i++){ if(match(questions.get(i),key)){ mode="all"; currentIndex=i; renderQuestion(); return; } } for(int i=0;i<=currentIndex;i++){ if(match(questions.get(i),key)){ mode="all"; currentIndex=i; renderQuestion(); return; } } toast("没有搜到相关题目"); }
    private boolean match(Question q,String key){ return (q.question+" "+q.a+" "+q.b+" "+q.c+" "+q.d+" "+q.explain).toLowerCase().contains(key); }
    private void jumpToQuestion(){ String s=jumpInput.getText().toString().trim(); if(s.length()==0){ toast("请输入题号"); return; } try{ int n=Integer.parseInt(s); if(n<1 || n>questions.size()){ toast("题号范围是 1-"+questions.size()); return; } currentIndex=n-1; mode="all"; examOrder.clear(); examPos=0; jumpInput.setText(""); renderQuestion(); }catch(Exception e){ toast("题号格式不正确"); } }
    private String modeName(){ if("wrong".equals(mode)) return "错题本"; if("favorite".equals(mode)) return "收藏题"; if("exam".equals(mode)) return "模拟考试"; return "全部题"; }
    private void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_SHORT).show(); }
    private int dp(int value){ return (int)(value*getResources().getDisplayMetrics().density+0.5f); }
}
