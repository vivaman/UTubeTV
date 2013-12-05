package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.sickboots.sickvideos.youtube.GoogleAccount;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

// Three things this hub does:
// Notification center
// Global data
// Cached preferences

public class ApplicationHub {
  private static ApplicationHub instance = null;
  private HashMap data;
  private Handler mainThreadHandler;
  private NotificationCenter notificationCenter;
  private PreferenceCache mPrefsCache;
  private GoogleAccount mGoogleAccount;
  private Context mApplicationContext;

  // public notifications
  public static final String APPLICATION_READY_NOTIFICATION = "application_ready";
  public static final String THEME_CHANGED = "theme_changed";

  private ApplicationHub(Context context) {
    mApplicationContext = context.getApplicationContext();
    notificationCenter = new NotificationCenter();
    mainThreadHandler = new Handler(Looper.getMainLooper());

    data = new HashMap();

    mPrefsCache = new PreferenceCache(mApplicationContext, new PreferenceCache.PreferenceCacheListener() {
      @Override
      public void prefChanged(String prefName) {
        if (prefName.equals(PreferenceCache.THEME_STYLE)) {
          sendNotification(THEME_CHANGED);
        }
      }
    });

    sendNotification(APPLICATION_READY_NOTIFICATION);
  }

  public static ApplicationHub instance(Context context) {
    // make sure this is never null
    if (context == null) {
      Util.log("### ApplicationHub instance: context null ###.");
      return null;
    }

    if (instance == null)
      instance = new ApplicationHub(context);

    return instance;
  }

  public static PreferenceCache preferences(Context context) {
    return instance(context).prefsCache();
  }

  public GoogleAccount googleAccount() {
    if (mGoogleAccount == null) {
      String accountName = preferences(mApplicationContext).getString(PreferenceCache.GOOGLE_ACCOUNT_PREF, null);

      mGoogleAccount = new GoogleAccount(mApplicationContext, accountName);
    }

    return mGoogleAccount;
  }

  public void setGoogleAccount(GoogleAccount account) {
    mGoogleAccount = account;
  }

  public PreferenceCache prefsCache() {
    return mPrefsCache;
  }

  // -------------------------------------
  // -------------------------------------
  // Global data

  public Object getData(String key) {
    return data.get(key);
  }

  public void setData(String key, Object value) {
    data.put(key, value);
  }

  // -------------------------------------
  // -------------------------------------
  // Notification center

  public void runOnMainThread(Runnable action) {
    if (action != null)
      mainThreadHandler.post(action);
  }

  public void addObserver(Observer observer) {
    notificationCenter.addObserver(observer);
  }

  public void deleteObserver(Observer observer) {
    notificationCenter.deleteObserver(observer);
  }

  public void sendNotification(final String message) {
    // always sends on main thread
    runOnMainThread(new Runnable() {
      @Override
      public void run() {
        notificationCenter.sendNotification(message);
      }
    });
  }

  // -------------------------------------
  // Notification center class

  class NotificationCenter extends Observable {
    protected void sendNotification(String message) {
      setChanged();
      notifyObservers(message);
    }
  }
}