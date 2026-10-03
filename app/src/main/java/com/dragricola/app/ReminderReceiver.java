package com.dragricola.app;
import android.app.*;
import android.content.*;
import android.os.Build;
import android.net.Uri;
import org.json.JSONObject;
import java.util.Map;

public class ReminderReceiver extends BroadcastReceiver {
 private static PendingIntent pending(Context c,String id,String title){Intent i=new Intent(c,ReminderReceiver.class);i.setAction("com.dragricola.REMINDER");i.setData(Uri.parse("dragricola://reminder/"+Uri.encode(id)));i.putExtra("id",id);i.putExtra("title",title);return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 public static void schedule(Context c,String id,String title,long time,boolean save){if(time<=System.currentTimeMillis())return;try{if(save){JSONObject item=new JSONObject();item.put("title",title);item.put("time",time);c.getSharedPreferences("reminders",0).edit().putString(id,item.toString()).apply();}AlarmManager manager=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pending(c,id,title));}catch(Exception e){android.util.Log.e("DrAgricola","Não foi possível agendar lembrete");}}
 public static void cancel(Context c,String id){((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).cancel(pending(c,id,""));c.getSharedPreferences("reminders",0).edit().remove(id).apply();}
 @Override public void onReceive(Context c,Intent i){if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())){for(Map.Entry<String,?> entry:c.getSharedPreferences("reminders",0).getAll().entrySet()){try{JSONObject item=new JSONObject(String.valueOf(entry.getValue()));schedule(c,entry.getKey(),item.getString("title"),item.getLong("time"),false);}catch(Exception ignored){}}return;}String id=i.getStringExtra("id"),title=i.getStringExtra("title");NotificationManager manager=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(new NotificationChannel("maintenance","Manutenção das máquinas",NotificationManager.IMPORTANCE_DEFAULT));Intent launch=new Intent(c,MainActivity.class);PendingIntent open=PendingIntent.getActivity(c,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification.Builder builder=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,"maintenance"):new Notification.Builder(c);builder.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Dr. Agrícola · Manutenção").setContentText(title).setStyle(new Notification.BigTextStyle().bigText(title)).setAutoCancel(true).setContentIntent(open);try{manager.notify(id==null?1:id.hashCode(),builder.build());}catch(SecurityException ignored){}if(id!=null)c.getSharedPreferences("reminders",0).edit().remove(id).apply();}
}
