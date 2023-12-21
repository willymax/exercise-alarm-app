package com.willymax.exercisealarm.data;

import android.content.Context;
import android.content.SharedPreferences;



public class SharedPref {

    private static final String FIRST_LAUNCH = "_.FIRST_LAUNCH";
    private static final String CLICK_OFFER = "_.MAX_CLICK_OFFER";
    private static final String CLICK_INTERS = "_.MAX_CLICK_INTERS ";
    private static final String CLICK_SWITCH = "_.MAX_CLICK_SWITCH";
    private static final String NEED_REGISTER = "_.NEED_REGISTER";
    private static final String FCM_PREF_KEY = "_.FCM_PREF_KEY";
//    private static final int MAX_CLICK_OFFER = BuildConfig.DEBUG ? 5 : 8;
//    private static final int MAX_CLICK_INTERS = BuildConfig.DEBUG ? 5 : 8;
    private Context context;
    private SharedPreferences sharedPreferences;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean flag) {
        sharedPreferences.edit().putBoolean(FIRST_LAUNCH, flag).apply();
    }

//    public boolean actionClickOffer() {
//        int current = sharedPreferences.getInt(CLICK_OFFER, 1);
//        boolean is_reset = false;
//        if (current < MAX_CLICK_OFFER) {
//            current++;
//        } else {
//            current = 1;
//            is_reset = true;
//        }
//        sharedPreferences.edit().putInt(CLICK_OFFER, current).apply();
//        return is_reset;
//    }

//    public boolean actionClickInters() {
//        int current = sharedPreferences.getInt(CLICK_INTERS, 1);
//        boolean is_reset = false;
//        if (current < MAX_CLICK_INTERS) {
//            current++;
//        } else {
//            current = 1;
//            is_reset = true;
//        }
//        sharedPreferences.edit().putInt(CLICK_INTERS, current).apply();
//        return is_reset;
//    }

    public boolean getClickSwitch() {
        return sharedPreferences.getBoolean(CLICK_SWITCH, true);
    }

    public void setClickSwitch(Boolean val) {
        sharedPreferences.edit().putBoolean(CLICK_SWITCH, val).apply();
    }

    public String getFcmRegId() {
        return sharedPreferences.getString(FCM_PREF_KEY, null);
    }

    public void setFcmRegId(String fcmRegId) {
        sharedPreferences.edit().putString(FCM_PREF_KEY, fcmRegId).apply();
    }

    public boolean isNeedRegister() {
        return sharedPreferences.getBoolean(NEED_REGISTER, true);
    }

    public void setNeedRegister(boolean value) {
        sharedPreferences.edit().putBoolean(NEED_REGISTER, value).apply();
    }

    public boolean isSubscribeNotification() {
        return sharedPreferences.getBoolean("SUBSCRIBE_NOTIFICATION", false);
    }
    
    public void setSubscribeNotification(boolean value) {
        sharedPreferences.edit().putBoolean("SUBSCRIBE_NOTIFICATION", value).apply();
    }

    public boolean isGDPR() {
        return sharedPreferences.getBoolean("GDPR", true);
    }

    public void setGDPR(boolean value) {
        sharedPreferences.edit().putBoolean("GDPR", value).apply();
    }

    /**
     * To save dialog permission state
     */
    public void setNeverAskAgain(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getNeverAskAgain(String key) {
        return sharedPreferences.getBoolean(key, false);
    }
}
