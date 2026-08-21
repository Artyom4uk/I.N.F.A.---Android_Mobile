package ru.infa.mobile;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final String PREFS = "infa_prefs";
    private static final String NAME = "name";
    private static final int BG = Color.rgb(5, 7, 13);
    private static final int CARD = Color.rgb(14, 18, 28);
    private static final int CARD_2 = Color.rgb(18, 23, 35);
    private static final int WHITE = Color.rgb(242, 245, 252);
    private static final int MUTED = Color.rgb(145, 155, 176);
    private static final int CYAN = Color.rgb(70, 215, 255);
    private static final int PURPLE = Color.rgb(164, 108, 255);
    private static final int GREEN = Color.rgb(72, 224, 154);

    private SharedPreferences prefs;
    private LinearLayout root;
    private EditText command;
    private TextView greeting, stateText;
    private OrbView orb;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        if (!prefs.contains(NAME)) showOnboarding(); else showHome();
    }

    private GradientDrawable bg(int color, float radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d;
    }
    private GradientDrawable stroke(int fill, int line, float radius) {
        GradientDrawable d = bg(fill, radius); d.setStroke(1, line); return d;
    }
    private TextView text(String s, float size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private Button button(String label) {
        Button b = new Button(this); b.setText(label); b.setTextColor(WHITE); b.setTextSize(13); b.setAllCaps(false);
        b.setGravity(Gravity.CENTER); b.setPadding(8, 0, 8, 0); b.setBackground(stroke(CARD_2, Color.rgb(35, 43, 60), 24)); return b;
    }
    private LinearLayout page() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(20, 18, 20, 18); root.setBackgroundColor(BG);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(root); setContentView(scroll); return root;
    }
    private void add(LinearLayout box, View v, int h, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, h); p.topMargin = top; box.addView(v, p);
    }

    private void showOnboarding() {
        LinearLayout p = page();
        Space sp = new Space(this); add(p, sp, 35, 0);
        TextView logo = text("I.N.F.A.", 38, WHITE); logo.setTypeface(null, 1); logo.setGravity(Gravity.CENTER); add(p, logo, 55, 0);
        TextView sub = text("Intelligent Network & Functional Assistant", 13, MUTED); sub.setGravity(Gravity.CENTER); add(p, sub, 32, 0);
        orb = new OrbView(this); add(p, orb, 260, 18);
        TextView welcome = text("Добро пожаловать", 25, WHITE); welcome.setTypeface(null, 1); welcome.setGravity(Gravity.CENTER); add(p, welcome, 42, 4);
        TextView desc = text("Ваш персональный помощник для телефона.\nКоманды, файлы, заметки, поиск и автоматизация — в одном месте.", 14, MUTED); desc.setGravity(Gravity.CENTER); add(p, desc, 70, 0);
        LinearLayout nameCard = new LinearLayout(this); nameCard.setPadding(18, 0, 18, 0); nameCard.setGravity(Gravity.CENTER_VERTICAL); nameCard.setBackground(stroke(CARD, Color.rgb(38, 47, 66), 22));
        EditText name = new EditText(this); name.setHint("Как вас зовут?"); name.setHintTextColor(MUTED); name.setTextColor(WHITE); name.setTextSize(16); name.setSingleLine(true); name.setBackgroundColor(Color.TRANSPARENT); nameCard.addView(name, new LinearLayout.LayoutParams(0, -1, 1));
        add(p, nameCard, 58, 12);
        Button start = button("Начать  →"); start.setTextSize(15); start.setTextColor(Color.WHITE); start.setBackground(gradient(CYAN, PURPLE, 26));
        add(p, start, 58, 14);
        TextView privacy = text("Данные профиля хранятся локально на телефоне.", 11, Color.rgb(105, 115, 135)); privacy.setGravity(Gravity.CENTER); add(p, privacy, 30, 10);
        start.setOnClickListener(v -> { String n = name.getText().toString().trim(); if(n.isEmpty()){ name.setError("Введите имя"); return; } prefs.edit().putString(NAME,n).apply(); showHome(); });
    }

    private GradientDrawable gradient(int a, int b, float radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a,b}); d.setCornerRadius(radius); return d;
    }

    private void showHome() {
        LinearLayout p = page();
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("I.N.F.A.", 27, WHITE); logo.setTypeface(null, 1); top.addView(logo, new LinearLayout.LayoutParams(0, 55, 1));
        TextView online = text("● Онлайн", 11, GREEN); online.setGravity(Gravity.CENTER); top.addView(online, new LinearLayout.LayoutParams(75, 40));
        Button help = button("?"); help.setTextSize(17); help.setOnClickListener(v -> showHelp()); top.addView(help, new LinearLayout.LayoutParams(48,48));
        Button settings = button("⚙"); settings.setTextSize(18); settings.setOnClickListener(v -> showSettings()); top.addView(settings, new LinearLayout.LayoutParams(48,48));
        add(p, top, 55, 0);

        greeting = text("Здравствуйте, " + prefs.getString(NAME,"друг") + "!", 25, WHITE); greeting.setTypeface(null,1); add(p,greeting,38,14);
        TextView sub = text("Что хотите сделать сегодня?",14,MUTED); add(p,sub,28,0);

        orb = new OrbView(this); add(p,orb,225,8);
        stateText = text("Готова к работе", 14, MUTED); stateText.setGravity(Gravity.CENTER); add(p,stateText,28,0);

        LinearLayout input = new LinearLayout(this); input.setGravity(Gravity.CENTER_VERTICAL); input.setPadding(8,4,8,4); input.setBackground(stroke(CARD, Color.rgb(42,52,73), 28));
        command = new EditText(this); command.setHint("Спросите I.N.F.A. или введите команду"); command.setHintTextColor(MUTED); command.setTextColor(WHITE); command.setTextSize(14); command.setSingleLine(true); command.setBackgroundColor(Color.TRANSPARENT); input.addView(command,new LinearLayout.LayoutParams(0,58,1));
        Button voice=button("🎙"); voice.setTextSize(18); voice.setOnClickListener(v->voiceInput()); input.addView(voice,new LinearLayout.LayoutParams(50,50));
        Button send=button("↑"); send.setTextSize(20); send.setBackground(gradient(CYAN,PURPLE,24)); send.setOnClickListener(v->runCommand(command.getText().toString())); input.addView(send,new LinearLayout.LayoutParams(50,50));
        add(p,input,66,12);

        TextView qt=text("Возможности",14,MUTED); add(p,qt,28,18);
        LinearLayout row1=new LinearLayout(this); row1.setGravity(Gravity.CENTER); addTiles(row1,new String[]{"◉\nАссистент","▣\nФайлы","⌘\nАвтоматизация"},new View.OnClickListener[]{v->assistant(),v->files(),v->automation()}); add(p,row1,88,0);
        LinearLayout row2=new LinearLayout(this); row2.setGravity(Gravity.CENTER); addTiles(row2,new String[]{"✎\nПамять","⌕\nИнтернет","⚙\nСистема"},new View.OnClickListener[]{v->notes(),v->webSearch(),v->phoneInfo()}); add(p,row2,88,8);

        TextView hint=text("Нажмите «?» чтобы увидеть все функции I.N.F.A.",12,Color.rgb(95,105,125)); hint.setGravity(Gravity.CENTER); add(p,hint,30,12);
        animateIn(p);
    }

    private void addTiles(LinearLayout row,String[] labels,View.OnClickListener[] actions){ for(int i=0;i<labels.length;i++){Button b=button(labels[i]); b.setTextSize(12); b.setOnClickListener(actions[i]); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,82,1); if(i>0)lp.leftMargin=8; row.addView(b,lp);} }

    private void animateIn(View v){ v.setAlpha(0f); v.animate().alpha(1f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start(); }

    private void assistant(){ new AlertDialog.Builder(this).setTitle("◉ I.N.F.A. Assistant").setMessage("Я готова принимать текстовые и голосовые команды.\n\nПримеры:\n• «Покажи информацию о телефоне»\n• «Открой Wi-Fi»\n• «Найди информацию о космосе»\n• «Создай заметку»\n• «Открой настройки»").setPositiveButton("Понятно",null).show(); }
    private void automation(){ new AlertDialog.Builder(this).setTitle("⌘ Автоматизация").setMessage("Здесь появятся сценарии I.N.F.A.: утро, учеба, ночь, запуск приложений и другие действия.\n\nМодуль уже заложен в архитектуру — следующим этапом добавим конструктор сценариев.").setPositiveButton("Ок",null).show(); }
    private void files(){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("*/*"); i.addCategory(Intent.CATEGORY_OPENABLE); try{startActivityForResult(i,200);}catch(Exception e){toast("Файловый менеджер недоступен");} }

    private void voiceInput(){ Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ru-RU"); i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Говорите с I.N.F.A."); try{stateText.setText("Слушаю вас…"); orb.setMode(1); startActivityForResult(i,100);}catch(Exception e){toast("Голосовой ввод недоступен");} }
    @Override protected void onActivityResult(int r,int c,Intent d){ super.onActivityResult(r,c,d); if(r==100){ if(c==RESULT_OK&&d!=null){ArrayList<String>x=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); if(x!=null&&!x.isEmpty()){command.setText(x.get(0));runCommand(x.get(0));}} else {stateText.setText("Готова к работе");orb.setMode(0);} } }

    private void runCommand(String raw){ String s=raw==null?"":raw.trim().toLowerCase(Locale.ROOT); if(s.isEmpty())return; stateText.setText("Обрабатываю запрос…"); orb.setMode(2);
        if(s.contains("памят")||s.contains("характеристик")||s.contains("телефон")){phoneInfo();return;}
        if(s.contains("замет")||s.startsWith("запиши")){notes();return;}
        if(s.contains("дневник")){diary();return;}
        if(s.contains("напомин")){reminder();return;}
        if(s.contains("интернет")||s.contains("найди")||s.contains("поищи")){webSearch(s);return;}
        if(s.contains("wi-fi")||s.contains("вайфай")){startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));return;}
        if(s.contains("bluetooth")||s.contains("блютуз")){startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));return;}
        if(s.contains("настройк")){startActivity(new Intent(Settings.ACTION_SETTINGS));return;}
        if(s.contains("фонар")){ toast("Управление фонариком добавим следующим модулем."); finishState(); return; }
        toast("Команда пока не подключена. Откройте «?» — там список доступных функций."); finishState();
    }
    private void finishState(){stateText.setText("Готова к работе");orb.setMode(0);}

    private EditText field(String hint){ EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(MUTED);e.setTextColor(WHITE);e.setTextSize(15);e.setPadding(18,12,18,12);e.setMinLines(3);e.setBackground(stroke(CARD,Color.rgb(38,47,66),20));return e; }
    private void notes(){ final EditText e=field("Текст заметки");e.setText(prefs.getString("note",""));new AlertDialog.Builder(this).setTitle("✎ Память").setMessage("Быстрая заметка").setView(e).setPositiveButton("Сохранить",(d,w)->{prefs.edit().putString("note",e.getText().toString()).apply();toast("Сохранено локально");finishState();}).setNegativeButton("Отмена",(d,w)->finishState()).show(); }
    private void diary(){ final EditText e=field("Что произошло сегодня?");e.setText(prefs.getString("diary",""));new AlertDialog.Builder(this).setTitle("📔 Дневник").setView(e).setPositiveButton("Сохранить",(d,w)->{String date=new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date());prefs.edit().putString("diary",date+"\n"+e.getText()).apply();toast("Запись сохранена");finishState();}).setNegativeButton("Отмена",(d,w)->finishState()).show(); }
    private void reminder(){final EditText e=field("Текст напоминания");new AlertDialog.Builder(this).setTitle("⏰ Напоминание").setView(e).setPositiveButton("Через 1 час",(d,w)->schedule(e.getText().toString())).setNegativeButton("Отмена",(d,w)->finishState()).show();}
    private void schedule(String s){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},77);Intent i=new Intent(this,ReminderReceiver.class);i.putExtra("text",s);PendingIntent pi=PendingIntent.getBroadcast(this,(int)System.currentTimeMillis(),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+3600000,pi);toast("Напоминание поставлено");finishState();}

    private void calculator(){final EditText e=field("Например: 12 * 8 + 4");e.setSingleLine();new AlertDialog.Builder(this).setTitle("⌗ Калькулятор").setView(e).setPositiveButton("Посчитать",(d,w)->{try{toast("= "+format(new Parser(e.getText().toString()).parse()));}catch(Exception ex){toast("Не удалось посчитать");}finishState();}).setNegativeButton("Отмена",(d,w)->finishState()).show();}
    private String format(double x){if(x==Math.rint(x))return Long.toString((long)x);return String.format(Locale.US,"%.6f",x).replaceAll("0+$","").replaceAll("\\.$","");}
    private static class Parser{String s;int p;Parser(String s){this.s=s.replace(" ","");}double parse(){double v=expr();if(p<s.length())throw new RuntimeException();return v;}double expr(){double v=term();while(p<s.length()){char c=s.charAt(p);if(c!='+'&&c!='-')break;p++;double n=term();v=c=='+'?v+n:v-n;}return v;}double term(){double v=factor();while(p<s.length()){char c=s.charAt(p);if(c!='*'&&c!='/')break;p++;double n=factor();v=c=='*'?v*n:v/n;}return v;}double factor(){if(p<s.length()&&s.charAt(p)=='-'){p++;return-factor();}if(p<s.length()&&s.charAt(p)=='('){p++;double v=expr();if(p>=s.length()||s.charAt(p)!=')')throw new RuntimeException();p++;return v;}int st=p;while(p<s.length()&&(Character.isDigit(s.charAt(p))||s.charAt(p)=='.'))p++;if(st==p)throw new RuntimeException();return Double.parseDouble(s.substring(st,p));}}

    private void phoneInfo(){StatFsCompat info=new StatFsCompat(getFilesDir().getAbsolutePath());String model=Build.MANUFACTURER+" "+Build.MODEL;String msg="Модель  ·  "+model+"\nAndroid  ·  "+Build.VERSION.RELEASE+" / API "+Build.VERSION.SDK_INT+"\nХранилище  ·  "+info.free+" ГБ свободно из "+info.total+" ГБ\n\nСледующий модуль: батарея, RAM, экран, сеть и сенсоры.";new AlertDialog.Builder(this).setTitle("📱 Система").setMessage(msg).setPositiveButton("Ок",(d,w)->finishState()).show();}
    private static class StatFsCompat{long free,total;StatFsCompat(String p){android.os.StatFs s=new android.os.StatFs(p);free=s.getAvailableBytes()/1073741824L;total=s.getTotalBytes()/1073741824L;}}
    private void webSearch(){webSearch("");}
    private void webSearch(String initial){final EditText e=field("Что найти в интернете?");if(initial.startsWith("найди")||initial.startsWith("поищи"))e.setText(initial.replaceFirst("^(найди|поищи)\\s*",""));new AlertDialog.Builder(this).setTitle("⌕ Интернет").setView(e).setPositiveButton("Искать",(d,w)->{String q=e.getText().toString().trim();if(!q.isEmpty())startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q="+Uri.encode(q))));finishState();}).setNegativeButton("Отмена",(d,w)->finishState()).show();}

    private void showHelp(){String msg="I.N.F.A. — единый командный центр телефона.\n\n◉ Ассистент — текст и голосовые команды.\n▣ Файлы — открытие документов и файлов.\n⌘ Автоматизация — сценарии и рутины.\n✎ Память — заметки и дневник.\n⌕ Интернет — поиск в браузере.\n⚙ Система — сведения о телефоне и настройки.\n⌗ Калькулятор — быстрые вычисления.\n⏰ Напоминания — локальные уведомления.\n\nПопробуйте: «Инфа, покажи информацию о телефоне».";new AlertDialog.Builder(this).setTitle("Все функции I.N.F.A.").setMessage(msg).setPositiveButton("Понятно",null).show();}
    private void showSettings(){new AlertDialog.Builder(this).setTitle("⚙ Настройки I.N.F.A.").setItems(new String[]{"Изменить имя","Сбросить знакомство","О приложении"},(d,w)->{if(w==0)changeName();else if(w==1){prefs.edit().clear().apply();showOnboarding();}else new AlertDialog.Builder(this).setTitle("I.N.F.A. 0.2.0").setMessage("Intelligent Network & Functional Assistant\n\nФутуристический интерфейс · локальные данные · без платных API").setPositiveButton("Ок",null).show();});}
    private void changeName(){EditText e=field("Имя");e.setSingleLine();e.setText(prefs.getString(NAME,""));new AlertDialog.Builder(this).setTitle("Ваше имя").setView(e).setPositiveButton("Сохранить",(d,w)->{prefs.edit().putString(NAME,e.getText().toString().trim()).apply();showHome();}).setNegativeButton("Отмена",null).show();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    public static class OrbView extends View {
        private Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private float phase=0; private int mode=0; private final Random rnd=new Random(7);
        public OrbView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);post(anim());}
        private Runnable anim(){return new Runnable(){public void run(){phase+=0.055f;invalidate();postDelayed(this,16);}};}
        public void setMode(int m){mode=m;invalidate();}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float cx=getWidth()/2f,cy=getHeight()/2f;float base=Math.min(getWidth(),getHeight())*.30f;int glow=mode==1?CYAN:mode==2?PURPLE:CYAN; for(int i=4;i>=1;i--){p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(16+i*8,glow,glow,255));p.setShadowLayer(24+i*8,0,0,glow);c.drawCircle(cx,cy,base+i*9+(float)Math.sin(phase*1.4+i)*3,p);}p.clearShadowLayer();
            p.setShader(new LinearGradient(cx-base,cy-base,cx+base,cy+base,new int[]{CYAN,PURPLE,CYAN},null,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,base,p);p.setShader(null);p.setColor(BG);c.drawCircle(cx,cy,base-7,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(Color.argb(120,CYAN,CYAN,255));for(int r=1;r<=3;r++)c.drawCircle(cx,cy,base+r*20+(float)Math.sin(phase+r)*2,p);
            p.setStrokeWidth(2);p.setColor(Color.argb(170,WHITE,WHITE,255));Path wave=new Path();for(int x=0;x<=getWidth();x+=8){float y=cy+base+30+(float)Math.sin(x*.07+phase)*((mode==1?15:8))+((x%32)-16)*.05f;if(x==0)wave.moveTo(x,y);else wave.lineTo(x,y);}c.drawPath(wave,p);p.setStyle(Paint.Style.FILL);p.setColor(WHITE);p.setShadowLayer(18,0,0,PURPLE);c.drawCircle(cx,cy,Math.max(7,base*.12f),p);p.clearShadowLayer();}
    }
}
