package com.dragricola.app;
import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.webkit.*;
import androidx.core.content.FileProvider;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
 private WebView webView; private ValueCallback<Uri[]> fileCallback; private TextToSpeech tts; private boolean ttsReady, notificationRequested; private String pendingBackup; private static final int FILE=200, VOICE=201, BACKUP=202;
 @Override public void onCreate(Bundle state){super.onCreate(state);webView=new WebView(this);setContentView(webView);WebSettings settings=webView.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(true);settings.setAllowContentAccess(true);webView.addJavascriptInterface(new Bridge(),"Android");webView.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){Uri u=request.getUrl();if("https".equals(u.getScheme())){try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){error("Não consegui abrir este link.");}return true;}return !"file".equals(u.getScheme());}});webView.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> callback,FileChooserParams params){if(fileCallback!=null)fileCallback.onReceiveValue(null);fileCallback=callback;try{startActivityForResult(params.createIntent(),FILE);}catch(Exception e){fileCallback.onReceiveValue(null);fileCallback=null;error("Não consegui abrir os arquivos do aparelho.");return false;}return true;}});tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS){ttsReady=true;tts.setLanguage(new Locale("pt","BR"));}});webView.loadUrl("file:///android_asset/index.html");}
 private void js(String function,String text){runOnUiThread(()->webView.evaluateJavascript(function+"("+JSONObject.quote(text)+")",null));}
 private void error(String text){js("toast",text);}
 private void voice(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},301);return;}Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"pt-BR");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Conte o problema da máquina");try{startActivityForResult(i,VOICE);}catch(Exception e){error("Reconhecimento de voz indisponível. Use o microfone do teclado.");}}
 public class Bridge {
  @JavascriptInterface public void startVoice(){runOnUiThread(()->voice());}
  @JavascriptInterface public void speak(String text){runOnUiThread(()->{if(!ttsReady){error("A voz ainda não está pronta. Tente novamente.");return;}tts.stop();for(int offset=0;offset<text.length();offset+=3900){tts.speak(text.substring(offset,Math.min(text.length(),offset+3900)),TextToSpeech.QUEUE_ADD,null,"dr-answer-"+offset);}});}
  @JavascriptInterface public void shareText(String text){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"Compartilhar"));});}
  @JavascriptInterface public void scheduleReminder(String id,String title,long time){ReminderReceiver.schedule(MainActivity.this,id,title,time,true);if(Build.VERSION.SDK_INT>=33&&!notificationRequested&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){notificationRequested=true;runOnUiThread(()->requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},302));}}
  @JavascriptInterface public void cancelReminder(String id){ReminderReceiver.cancel(MainActivity.this,id);}
  @JavascriptInterface public void openPdf(String name,String encoded){runOnUiThread(()->{try{byte[] bytes=android.util.Base64.decode(encoded,android.util.Base64.DEFAULT);File folder=new File(getCacheDir(),"shared");folder.mkdirs();File file=new File(folder,"manual.pdf");try(FileOutputStream out=new FileOutputStream(file)){out.write(bytes);}Uri uri=FileProvider.getUriForFile(MainActivity.this,getPackageName()+".files",file);Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(uri,"application/pdf");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Abrir manual"));}catch(Exception e){error("Instale um leitor de PDF para abrir o manual.");}});}
  @JavascriptInterface public void exportBackup(String name,String content){runOnUiThread(()->{pendingBackup=content;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,name);try{startActivityForResult(i,BACKUP);}catch(Exception e){pendingBackup=null;error("Não consegui abrir o local para salvar o backup.");}});}
 }
 @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){super.onRequestPermissionsResult(code,permissions,results);if(code==301){if(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED)voice();else error("Microfone não autorizado. Você pode digitar a pergunta.");}if(code==302&&!(results.length>0&&results[0]==PackageManager.PERMISSION_GRANTED))error("Lembrete salvo. Ative as notificações nas configurações para receber alertas.");}
 @Override protected void onActivityResult(int code,int result,Intent data){super.onActivityResult(code,result,data);if(code==FILE&&fileCallback!=null){fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));fileCallback=null;}if(code==VOICE&&result==RESULT_OK&&data!=null){ArrayList<String> words=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(words!=null&&!words.isEmpty())js("onVoiceResult",words.get(0));}if(code==BACKUP){if(result==RESULT_OK&&data!=null&&data.getData()!=null&&pendingBackup!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(pendingBackup.getBytes(java.nio.charset.StandardCharsets.UTF_8));error("Backup salvo. Guarde este arquivo.");}catch(Exception e){error("Não consegui salvar o backup.");}}pendingBackup=null;}}
 @Override public void onBackPressed(){webView.evaluateJavascript("show('home')",null);}
 @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
