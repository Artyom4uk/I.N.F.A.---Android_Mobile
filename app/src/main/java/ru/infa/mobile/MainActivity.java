package ru.infa.mobile;

import android.Manifest;
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
    private static final String PREFS="infa_prefs", NAME="name";
    private static final int BG=Color.rgb(5,7,13), CARD=Color.rgb(14,18,28), CARD2=Color.rgb(18,23,35);
    private static final int WHITE=Color.rgb(242,245,252), MUTED=Color.rgb(145,155,176), CYAN=Color.rgb(70,215,255), PURPLE=Color.rgb(164,108,255), GREEN=Color.rgb(72,224,154);
    private SharedPreferences prefs;
    private LinearLayout root;
    private EditText command;
    private TextView stateText;
    private OrbView orb;

    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private GradientDrawable bg(int color,float r){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp((int)r));return d;}
    private GradientDrawable stroke(int fill,int line,float r){GradientDrawable d=bg(fill,r);d.setStroke(dp(1),line);return d;}
    private GradientDrawable gradient(int a,int b,float r){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp((int)r));return d;}
    private TextView text(String s,float size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setTextColor(WHITE);b.setTextSize(13);b.setAllCaps(false);b.setGravity(Gravity.CENTER);b.setPadding(dp(8),0,dp(8),0);b.setBackground(stroke(CARD2,Color.rgb(35,43,60),24));return b;}
    private LinearLayout page(){root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(18),dp(20),dp(18));root.setBackgroundColor(BG);ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);scroll.addView(root,new ScrollView.LayoutParams(-1,-2));setContentView(scroll);return root;}
    private void add(LinearLayout box,View v,int h,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(h));p.topMargin=dp(top);box.addView(v,p);}
    private LinearLayout lpRow(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);r.setGravity(Gravity.CENTER_VERTICAL);return r;}

    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences(PREFS,MODE_PRIVATE);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);if(!prefs.contains(NAME))showOnboarding();else showHome();}

    private void showOnboarding(){
        LinearLayout p=page(); add(p,new Space(this),18,0);
        TextView logo=text("I.N.F.A.",38,WHITE);logo.setTypeface(null,1);logo.setGravity(Gravity.CENTER);add(p,logo,55,0);
        TextView sub=text("Intelligent Network & Functional Assistant",13,MUTED);sub.setGravity(Gravity.CENTER);add(p,sub,30,0);
        orb=new OrbView(this);add(p,orb,235,12);
        TextView welcome=text("Добро пожаловать",25,WHITE);welcome.setTypeface(null,1);welcome.setGravity(Gravity.CENTER);add(p,welcome,40,2);
        TextView desc=text("Ваш персональный помощник для телефона.\nКоманды, файлы, заметки, поиск и автоматизация — в одном месте.",14,MUTED);desc.setGravity(Gravity.CENTER);add(p,desc,62,0);
        LinearLayout card=new LinearLayout(this);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(16),0,dp(16),0);card.setBackground(stroke(CARD,Color.rgb(38,47,66),22));
        EditText name=new EditText(this);name.setHint("Как вас зовут?");name.setHintTextColor(MUTED);name.setTextColor(WHITE);name.setTextSize(16);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);card.addView(name,new LinearLayout.LayoutParams(0,-1,1));add(p,card,56,10);
        Button start=button("Начать  →");start.setTextSize(15);start.setBackground(gradient(CYAN,PURPLE,26));add(p,start,56,12);
        TextView privacy=text("Данные профиля хранятся локально на телефоне.",11,Color.rgb(105,115,135));privacy.setGravity(Gravity.CENTER);add(p,privacy,26,8);
        start.setOnClickListener(v->{String n=name.getText().toString().trim();if(n.isEmpty()){name.setError("Введите имя");return;}prefs.edit().putString(NAME,n).apply();showHome();});
        animateIn(p);
    }

    private void showHome(){
        LinearLayout p=page();
        LinearLayout top=lpRow();
        TextView logo=text("I.N.F.A.",27,WHITE);logo.setTypeface(null,1);top.addView(logo,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView online=text("● Онлайн",11,GREEN);online.setGravity(Gravity.CENTER);top.addView(online,new LinearLayout.LayoutParams(dp(72),dp(42)));
        Button help=button("?");help.setTextSize(17);help.setOnClickListener(v->showHelp());top.addView(help,new LinearLayout.LayoutParams(dp(46),dp(46)));
        Button settings=button("⚙");settings.setTextSize(18);settings.setOnClickListener(v->showSettings());top.addView(settings,new LinearLayout.LayoutParams(dp(46),dp(46)));add(p,top,52,0);
        TextView greeting=text("Здравствуйте, "+prefs.getString(NAME,"друг")+"!",25,WHITE);greeting.setTypeface(null,1);add(p,greeting,38,12);
        add(p,text("Что хотите сделать сегодня?",14,MUTED),26,0);
        orb=new OrbView(this);add(p,orb,220,4);
        stateText=text("Готова к работе",14,MUTED);stateText.setGravity(Gravity.CENTER);add(p,stateText,28,0);
        LinearLayout input=lpRow();input.setPadding(dp(8),dp(4),dp(8),dp(4));input.setBackground(stroke(CARD,Color.rgb(42,52,73),28));
        command=new EditText(this);command.setHint("Спросите I.N.F.A. или введите команду");command.setHintTextColor(MUTED);command.setTextColor(WHITE);command.setTextSize(14);command.setSingleLine(true);command.setBackgroundColor(Color.TRANSPARENT);input.addView(command,new LinearLayout.LayoutParams(0,dp(56),1));
        Button voice=button("🎙");voice.setTextSize(18);voice.setOnClickListener(v->voiceInput());input.addView(voice,new LinearLayout.LayoutParams(dp(48),dp(48)));
        Button send=button("↑");send.setTextSize(20);send.setBackground(gradient(CYAN,PURPLE,24));send.setOnClickListener(v->runCommand(command.getText().toString()));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(48),dp(48));sp.leftMargin=dp(6);input.addView(send,sp);add(p,input,64,10);
        add(p,text("Возможности",14,MUTED),26,16);
        LinearLayout r1=lpRow();addTiles(r1,new String[]{"◉\nАссистент","▣\nФайлы","⌘\nАвтоматизация"},new View.OnClickListener[]{v->assistant(),v->files(),v->automation()});add(p,r1,82,0);
        LinearLayout r2=lpRow();addTiles(r2,new String[]{"✎\nПамять","⌕\nИнтернет","⚙\nСистема"},new View.OnClickListener[]{v->notes(),v->webSearch(),v->phoneInfo()});add(p,r2,82,8);
        LinearLayout r3=lpRow();addTiles(r3,new String[]{"⌗\nКалькулятор","⏰\nНапоминания","❔\nВсе функции"},new View.OnClickListener[]{v->calculator(),v->reminder(),v->showHelp()});add(p,r3,82,8);
        TextView hint=text("I.N.F.A. работает локально и не требует платного API.",11,Color.rgb(95,105,125));hint.setGravity(Gravity.CENTER);add(p,hint,28,14);
        animateIn(p);
    }

    private void addTiles(LinearLayout row,String[] labels,View.OnClickListener[] actions){for(int i=0;i<labels.length;i++){Button b=button(labels[i]);b.setTextSize(12);b.setOnClickListener(actions[i]);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(78),1);if(i>0)lp.leftMargin=dp(7);row.addView(b,lp);}}
    private void animateIn(View v){v.setAlpha(0f);v.animate().alpha(1f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();}

    private void assistant(){new AlertDialog.Builder(this).setTitle("◉ I.N.F.A. Assistant").setMessage("Текстовые и голосовые команды.\n\nПримеры:\n• Покажи информацию о телефоне\n• Открой Wi-Fi\n• Найди информацию о космосе\n• Создай заметку\n• Открой настройки").setPositiveButton("Понятно",null).show();}
    private void automation(){new AlertDialog.Builder(this).setTitle("⌘ Автоматизация").setMessage("Сценарии I.N.F.A. будут запускать набор действий одной командой: утро, учёба, ночь и другое.").setPositiveButton("Ок",null).show();}
    private void files(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);try{startActivityForResult(i,200);}catch(Exception e){toast("Файловый менеджер недоступен");}}
    private void voiceInput(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ru-RU");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Говорите с I.N.F.A.");try{stateText.setText("Слушаю вас…");orb.setMode(1);startActivityForResult(i,100);}catch(Exception e){toast("Голосовой ввод недоступен");}}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==100){if(c==RESULT_OK&&d!=null){ArrayList<String>x=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(x!=null&&!x.isEmpty()){command.setText(x.get(0));runCommand(x.get(0));}}else{stateText.setText("Готова к работе");orb.setMode(0);}}}
    private void runCommand(String raw){String s=raw==null?"":raw.trim().toLowerCase(Locale.ROOT);if(s.isEmpty())return;stateText.setText("Обрабатываю запрос…");orb.setMode(2);if(s.contains("памят")||s.contains("характеристик")||s.contains("телефон")){phoneInfo();return;}if(s.contains("замет")||s.startsWith("запиши")){notes();return;}if(s.contains("дневник")){diary();return;}if(s.contains("напомин")){reminder();return;}if(s.contains("кальк")||s.matches(".*[0-9]+\\s*[+\\-*/].*")){calculator();return;}if(s.contains("интернет")||s.contains("найди")||s.contains("поищи")){webSearch(s);return;}if(s.contains("wi-fi")||s.contains("вайфай")){startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));finishState();return;}if(s.contains("bluetooth")||s.contains("блютуз")){startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));finishState();return;}if(s.contains("настройк")){startActivity(new Intent(Settings.ACTION_SETTINGS));finishState();return;}toast("Команда пока не подключена. Нажмите «?» для списка функций.");finishState();}
    private void finishState(){if(stateText!=null)stateText.setText("Готова к работе");if(orb!=null)orb.setMode(0);}
    private EditText field(String hint){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(MUTED);e.setTextColor(WHITE);e.setTextSize(15);e.setPadding(dp(18),dp(12),dp(18),dp(12));e.setMinLines(3);e.setBackground(stroke(CARD,Color.rgb(38,47,66),20));return e;}
    private void notes(){final EditText e=field("Текст заметки");e.setText(prefs.getString("note",""));new AlertDialog.Builder(this).setTitle("✎ Память").setMessage("Быстрая заметка").setView(e).setPositiveButton("Сохранить",(d,w)->{prefs.edit().putString("note",e.getText().toString()).apply();toast("Сохранено локально");finishState();}).setNegativeButton("Отмена",(d,w)->finishState()).show();}
    private void diary(){final EditText e=field("Что произошло сегодня?");e.setText(prefs.getString("diary",""));new AlertDialog.Builder(this).setTitle("📔 Дневник").setView(e).setPositiveButton("Сохранить",(d,w)->{String date=new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date());prefs.edit().putString("diary",date+"\n"+e.getText()).apply();toast("Запись сохранена");finishState();}).setNegativeButton("Отмена",(d,w)->finishState()).show();}
    private void reminder(){final EditText e=field("Текст напоминания");new AlertDialog.Builder(this).setTitle("⏰ Напоминание").setView(e).setPositiveButton("Через 1 час",(d,w)->schedule(e.getText().toString())).setNegativeButton("Отмена",(d,w)->finishState()).show();}
    private void schedule(String s){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},77);Intent i=new Intent(this,ReminderReceiver.class);i.putExtra("text",s);PendingIntent pi=PendingIntent.getBroadcast(this,(int)System.currentTimeMillis(),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+3600000,pi);toast("Напоминание поставлено");finishState();}
    private void calculator(){final EditText e=field("Например: 12 * 8 + 4");e.setSingleLine();new AlertDialog.Builder(this).setTitle("⌗ Калькулятор").setView(e).setPositiveButton("Посчитать",(d,w)->{try{toast("= "+format(new Parser(e.getText().toString()).parse()));}catch(Exception ex){toast("Не удалось посчитать");}finishState();}).setNegativeButton("Отмена",(d,w)->finishState()).show();}
    private String format(double x){if(x==Math.rint(x))return Long.toString((long)x);return String.format(Locale.US,"%.6f",x).replaceAll("0+$","").replaceAll("\\.$","");}
    private static class Parser{String s;int p;Parser(String s){this.s=s.replace(" ","");}double parse(){double v=expr();if(p<s.length())throw new RuntimeException();return v;}double expr(){double v=term();while(p<s.length()){char c=s.charAt(p);if(c!='+'&&c!='-')break;p++;double n=term();v=c=='+'?v+n:v-n;}return v;}double term(){double v=factor();while(p<s.length()){char c=s.charAt(p);if(c!='*'&&c!='/')break;p++;double n=factor();v=c=='*'?v*n:v/n;}return v;}double factor(){if(p<s.length()&&s.charAt(p)=='-'){p++;return-factor();}if(p<s.length()&&s.charAt(p)=='('){p++;double v=expr();if(p>=s.length()||s.charAt(p)!=')')throw new RuntimeException();p++;return v;}int st=p;while(p<s.length()&&(Character.isDigit(s.charAt(p))||s.charAt(p)=='.'))p++;if(st==p)throw new RuntimeException();return Double.parseDouble(s.substring(st,p));}}
    private void phoneInfo(){StatFsCompat info=new StatFsCompat(getFilesDir().getAbsolutePath());String model=Build.MANUFACTURER+" "+Build.MODEL;String msg="Модель · "+model+"\nAndroid · "+Build.VERSION.RELEASE+" / API "+Build.VERSION.SDK_INT+"\nХранилище · "+info.free+" ГБ свободно из "+info.total+" ГБ\n\nСледующий модуль: батарея, RAM, экран, сеть и сенсоры.";new AlertDialog.Builder(this).setTitle("📱 Система").setMessage(msg).setPositiveButton("Ок",(d,w)->finishState()).show();}
    private static class StatFsCompat{long free,total;StatFsCompat(String p){android.os.StatFs s=new android.os.StatFs(p);free=s.getAvailableBytes()/1073741824L;total=s.getTotalBytes()/1073741824L;}}
    private void webSearch(){webSearch("");}
    private void webSearch(String initial){final EditText e=field("Что найти в интернете?");if(initial.startsWith("найди")||initial.startsWith("поищи"))e.setText(initial.replaceFirst("^(найди|поищи)\\s*",""));new AlertDialog.Builder(this).setTitle("⌕ Интернет").setView(e).setPositiveButton("Искать",(d,w)->{String q=e.getText().toString().trim();if(!q.isEmpty())startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+Uri.encode(q))));finishState();}).setNegativeButton("Отмена",(d,w)->finishState()).show();}
    private void showHelp(){String msg="I.N.F.A. — единый командный центр телефона.\n\n◉ Ассистент — текст и голос.\n▣ Файлы — открытие документов.\n⌘ Автоматизация — сценарии.\n✎ Память — заметки и дневник.\n⌕ Интернет — поиск.\n⚙ Система — сведения о телефоне.\n⌗ Калькулятор — вычисления.\n⏰ Напоминания — уведомления.\n\nПопробуйте: «Инфа, покажи информацию о телефоне».";new AlertDialog.Builder(this).setTitle("Все функции I.N.F.A.").setMessage(msg).setPositiveButton("Понятно",null).show();}
    private void showSettings(){new AlertDialog.Builder(this).setTitle("⚙ Настройки I.N.F.A.").setItems(new String[]{"Изменить имя","Сбросить знакомство","О приложении"},(d,w)->{if(w==0)changeName();else if(w==1){prefs.edit().clear().apply();showOnboarding();}else new AlertDialog.Builder(this).setTitle("I.N.F.A. 0.2.1").setMessage("Intelligent Network & Functional Assistant\n\nФутуристический интерфейс · локальные данные · без платных API").setPositiveButton("Ок",null).show();});}
    private void changeName(){EditText e=field("Имя");e.setSingleLine();e.setText(prefs.getString(NAME,""));new AlertDialog.Builder(this).setTitle("Ваше имя").setView(e).setPositiveButton("Сохранить",(d,w)->{prefs.edit().putString(NAME,e.getText().toString().trim()).apply();showHome();}).setNegativeButton("Отмена",null).show();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    public static class OrbView extends View{
        private Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private float phase=0;private int mode=0;
        public OrbView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);post(anim());}
        private Runnable anim(){return new Runnable(){public void run(){phase+=.055f;invalidate();postDelayed(this,16);}};}
        public void setMode(int m){mode=m;invalidate();}
        @Override protected void onDraw(Canvas c){float cx=getWidth()/2f,cy=getHeight()/2f;float base=Math.min(getWidth(),getHeight())*.30f;int glow=mode==1?CYAN:mode==2?PURPLE:CYAN;for(int i=4;i>=1;i--){p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(16+i*8,glow,glow,255));p.setShadowLayer(dp(20+i*6),0,0,glow);c.drawCircle(cx,cy,base+i*dp(8)+(float)Math.sin(phase*1.4+i)*dp(3),p);}p.clearShadowLayer();p.setShader(new LinearGradient(cx-base,cy-base,cx+base,cy+base,new int[]{CYAN,PURPLE,CYAN},null,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,base,p);p.setShader(null);p.setColor(BG);c.drawCircle(cx,cy,base-dp(7),p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(Color.argb(120,CYAN,CYAN,255));for(int r=1;r<=3;r++)c.drawCircle(cx,cy,base+r*dp(20)+(float)Math.sin(phase+r)*dp(2),p);p.setStrokeWidth(dp(2));p.setColor(Color.argb(170,WHITE,WHITE,255));Path wave=new Path();for(int x=0;x<=getWidth();x+=dp(8)){float y=cy+base+dp(30)+(float)Math.sin(x*.07+phase)*(mode==1?dp(15):dp(8));if(x==0)wave.moveTo(x,y);else wave.lineTo(x,y);}c.drawPath(wave,p);p.setStyle(Paint.Style.FILL);p.setColor(WHITE);p.setShadowLayer(dp(18),0,0,PURPLE);c.drawCircle(cx,cy,Math.max(dp(7),base*.12f),p);p.clearShadowLayer();}
    }
}