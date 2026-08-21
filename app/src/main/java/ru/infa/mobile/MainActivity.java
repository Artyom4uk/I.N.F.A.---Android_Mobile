package ru.infa.mobile;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final String PREFS = "infa_prefs";
    private static final String NAME = "name";
    private LinearLayout root, quickGrid;
    private TextView greeting, status;
    private EditText command;
    private SharedPreferences prefs;

    int bg = Color.rgb(11,13,16), card = Color.rgb(22,25,30), text = Color.rgb(238,242,247), muted = Color.rgb(155,164,176), accent = Color.rgb(138,180,248);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!prefs.contains(NAME)) showOnboarding(); else showHome();
    }

    private TextView tv(String s, float size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setPadding(0,0,0,0); return t;
    }
    private GradientDrawable round(int color, float r) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(r); return d; }
    private Button btn(String label) {
        Button b = new Button(this); b.setText(label); b.setTextColor(text); b.setTextSize(14); b.setAllCaps(false); b.setBackground(round(card, 28)); b.setPadding(8,0,8,0); return b;
    }

    private void base() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,22,24,20); root.setBackgroundColor(bg);
        setContentView(root);
    }

    private void showOnboarding() {
        base();
        Space top = new Space(this); root.addView(top, new LinearLayout.LayoutParams(1,80));
        TextView logo = tv("I.N.F.A.", 38, text); logo.setTypeface(null, 1); root.addView(logo);
        TextView sub = tv("Intelligent Network & Functional Assistant", 15, muted); root.addView(sub);
        root.addView(tv("\nПривет! Давай познакомимся.", 24, text));
        root.addView(tv("\nКак тебя зовут?", 16, muted));
        EditText name = new EditText(this); name.setHint("Например, Артём"); name.setTextColor(text); name.setHintTextColor(muted); name.setSingleLine(); name.setTextSize(18); name.setBackground(round(card, 22)); name.setPadding(20,4,20,4);
        root.addView(name, new LinearLayout.LayoutParams(-1,58));
        Button next = btn("Продолжить  →"); next.setBackground(round(accent, 28)); next.setTextColor(Color.rgb(8,12,18));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1,58); np.topMargin=18; root.addView(next,np);
        next.setOnClickListener(v -> { String n=name.getText().toString().trim(); if(n.isEmpty()){name.setError("Введите имя");return;} prefs.edit().putString(NAME,n).apply(); showHome(); });
    }

    private void showHome() {
        base();
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=tv("I.N.F.A.",28,text); logo.setTypeface(null,1); top.addView(logo,new LinearLayout.LayoutParams(0,60,1));
        Button settings=btn("⚙"); settings.setTextSize(22); settings.setOnClickListener(v->showSettings()); top.addView(settings,new LinearLayout.LayoutParams(58,58)); root.addView(top);
        greeting=tv("Добрый день, "+prefs.getString(NAME,"друг")+" 👋",24,text); greeting.setTypeface(null,1); LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,70); gp.topMargin=8; root.addView(greeting,gp);

        command=new EditText(this); command.setHint("Чем помочь?"); command.setHintTextColor(muted); command.setTextColor(text); command.setTextSize(17); command.setSingleLine(); command.setPadding(20,0,10,0); command.setBackground(round(card,26));
        LinearLayout cmdRow=new LinearLayout(this); cmdRow.setGravity(Gravity.CENTER_VERTICAL); cmdRow.setBackground(round(card,26)); cmdRow.addView(command,new LinearLayout.LayoutParams(0,62,1));
        Button voice=btn("🎤"); voice.setTextSize(20); voice.setOnClickListener(v->voiceInput()); cmdRow.addView(voice,new LinearLayout.LayoutParams(58,58));
        Button send=btn("➤"); send.setTextSize(20); send.setOnClickListener(v->runCommand(command.getText().toString())); cmdRow.addView(send,new LinearLayout.LayoutParams(58,58)); root.addView(cmdRow);

        TextView q=tv("\nБыстрые действия",15,muted); root.addView(q);
        quickGrid=new LinearLayout(this); quickGrid.setOrientation(LinearLayout.VERTICAL); root.addView(quickGrid);
        addRow(new String[]{"📝\nЗаметки","🧮\nКалькулятор","📱\nТелефон"}, new View.OnClickListener[]{v->notes(),v->calculator(),v->phoneInfo()});
        addRow(new String[]{"📔\nДневник","⏰\nНапоминание","🌐\nИнтернет"}, new View.OnClickListener[]{v->diary(),v->reminder(),v->webSearch()});
        status=tv("\nГотова. Напиши или скажи, что нужно.",14,muted); root.addView(status);
    }

    private void addRow(String[] labels, View.OnClickListener[] actions){ LinearLayout row=new LinearLayout(this); row.setPadding(0,6,0,0); for(int i=0;i<labels.length;i++){Button b=btn(labels[i]); b.setGravity(Gravity.CENTER); b.setTextSize(14); b.setOnClickListener(actions[i]); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,72,1); if(i>0)p.leftMargin=8; row.addView(b,p);} quickGrid.addView(row); }

    private void voiceInput(){ Intent i=new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE,"ru-RU"); i.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT,"Скажите команду"); try{startActivityForResult(i,100);}catch(Exception e){toast("Голосовой ввод недоступен");} }
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d); if(r==100&&c==RESULT_OK&&d!=null){ArrayList<String> x=d.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS); if(x!=null&&!x.isEmpty()){command.setText(x.get(0));runCommand(x.get(0));}}}

    private void runCommand(String raw){ String s=raw==null?"":raw.trim().toLowerCase(Locale.ROOT); if(s.isEmpty())return;
        if(s.contains("памят")||s.contains("характеристик")||s.contains("телефон")){phoneInfo();return;}
        if(s.contains("калькулятор")||s.matches(".*\\d+[+*/-]\\d+.*")){calculator();return;}
        if(s.contains("замет")||s.startsWith("запиши")){notes();return;}
        if(s.contains("дневник")){diary();return;}
        if(s.contains("напомин")){reminder();return;}
        if(s.contains("интернет")||s.contains("найди")||s.contains("поищи")){webSearch(s);return;}
        if(s.contains("wi-fi")||s.contains("вайфай")){startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));return;}
        if(s.contains("bluetooth")||s.contains("блютуз")){startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));return;}
        if(s.contains("настройк")){startActivity(new Intent(Settings.ACTION_SETTINGS));return;}
        toast("Я пока не знаю эту команду. Но её можно добавить в Command Engine.");
    }

    private void notes(){ final EditText e=field("Текст заметки"); new AlertDialog.Builder(this).setTitle("📝 Заметки").setView(e).setPositiveButton("Сохранить",(d,w)->{prefs.edit().putString("note",e.getText().toString()).apply();toast("Заметка сохранена");}).setNegativeButton("Отмена",null).show(); }
    private void diary(){ final EditText e=field("Что произошло сегодня?"); String old=prefs.getString("diary","" ); e.setText(old); new AlertDialog.Builder(this).setTitle("📔 Дневник").setView(e).setPositiveButton("Сохранить",(d,w)->{String date=new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date());prefs.edit().putString("diary",date+"\n"+e.getText()).apply();toast("Запись добавлена");}).setNegativeButton("Отмена",null).show(); }
    private EditText field(String hint){EditText e=new EditText(this);e.setHint(hint);e.setMinLines(4);e.setTextColor(text);e.setHintTextColor(muted);e.setPadding(18,12,18,12);return e;}

    private void calculator(){ final EditText e=field("Например: 12 * 8 + 4"); e.setSingleLine(); new AlertDialog.Builder(this).setTitle("🧮 Калькулятор").setView(e).setPositiveButton("Посчитать",(d,w)->{try{toast("= "+format(eval(e.getText().toString())));}catch(Exception ex){toast("Не удалось посчитать");}}).setNegativeButton("Отмена",null).show(); }
    private double eval(String x){return new Parser(x).parse();}
    private String format(double x){if(x==Math.rint(x))return Long.toString((long)x);return String.format(Locale.US,"%.6f",x).replaceAll("0+$","").replaceAll("\\.$","");}
    private static class Parser{String s;int p=0;Parser(String s){this.s=s.replace(" ","");}double parse(){double v=expr();if(p<s.length())throw new RuntimeException();return v;}double expr(){double v=term();while(p<s.length()){char c=s.charAt(p);if(c!='+'&&c!='-')break;p++;double n=term();v=c=='+'?v+n:v-n;}return v;}double term(){double v=factor();while(p<s.length()){char c=s.charAt(p);if(c!='*'&&c!='/')break;p++;double n=factor();v=c=='*'?v*n:v/n;}return v;}double factor(){if(p<s.length()&&s.charAt(p)=='-'){p++;return-factor();}if(p<s.length()&&s.charAt(p)=='('){p++;double v=expr();if(p>=s.length()||s.charAt(p)!=')')throw new RuntimeException();p++;return v;}int st=p;while(p<s.length()&&(Character.isDigit(s.charAt(p))||s.charAt(p)=='.'))p++;if(st==p)throw new RuntimeException();return Double.parseDouble(s.substring(st,p));}}

    private void phoneInfo(){android.os.StatFs st=new android.os.StatFs(getFilesDir().getAbsolutePath());long free=st.getAvailableBytes()/1073741824L,total=st.getTotalBytes()/1073741824L;String model=Build.MANUFACTURER+" "+Build.MODEL;String info="Модель: "+model+"\nAndroid: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")\nСвободно в хранилище: "+free+" ГБ из "+total+" ГБ\n\nЭто базовая информация; дальше добавим батарею, RAM, экран и другие данные.";new AlertDialog.Builder(this).setTitle("📱 Телефон").setMessage(info).setPositiveButton("Ок",null).show();}

    private void webSearch(){webSearch("");}
    private void webSearch(String initial){ final EditText e=field("Что найти в интернете?"); if(initial.startsWith("найди")||initial.startsWith("поищи")){e.setText(initial.replaceFirst("^(найди|поищи)\\s*",""));}new AlertDialog.Builder(this).setTitle("🌐 Интернет").setView(e).setPositiveButton("Искать",(d,w)->{String q=e.getText().toString().trim();if(!q.isEmpty())startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q="+Uri.encode(q))));}).setNegativeButton("Отмена",null).show();}

    private void reminder(){ final EditText e=field("Текст напоминания");new AlertDialog.Builder(this).setTitle("⏰ Напоминание").setView(e).setPositiveButton("Поставить через 1 час",(d,w)->schedule(e.getText().toString())).setNegativeButton("Отмена",null).show(); }
    private void schedule(String text){ if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},77);Intent i=new Intent(this,ReminderReceiver.class);i.putExtra("text",text);PendingIntent pi=PendingIntent.getBroadcast(this, (int)System.currentTimeMillis(), i, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);((AlarmManager)getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+3600000,pi);toast("Напоминание поставлено на час вперёд");}

    private void showSettings(){new AlertDialog.Builder(this).setTitle("⚙️ Настройки I.N.F.A.").setItems(new String[]{"Изменить имя","О приложении","Сбросить настройку"},(d,w)->{if(w==0)changeName();else if(w==1)new AlertDialog.Builder(this).setTitle("I.N.F.A. 0.1.0").setMessage("Intelligent Network & Functional Assistant\n\nПервая сборка. Без платных API.").setPositiveButton("Ок",null).show();else{prefs.edit().clear().apply();showOnboarding();}}).show();}
    private void changeName(){EditText e=field("Имя");e.setSingleLine();e.setText(prefs.getString(NAME,""));new AlertDialog.Builder(this).setTitle("Ваше имя").setView(e).setPositiveButton("Сохранить",(d,w)->{prefs.edit().putString(NAME,e.getText().toString().trim()).apply();showHome();}).setNegativeButton("Отмена",null).show();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
