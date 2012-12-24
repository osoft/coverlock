package net.zalio.android.cso;

import android.util.Log;

public class MyLog {
    static final private String TAG = "CSO";
    static private boolean iFlag;
    static private boolean dFlag;
    static private boolean wFlag;
    static private boolean eFlag;
    static private boolean vFlag;
    
    static{
        iFlag = dFlag = wFlag = eFlag = vFlag = true;
    }
    
    public static final void i(String tag, String content){
        if(iFlag)
            Log.i(TAG, tag + ": " + content);
    }
    
    public static final void d(String tag, String content){
        if(dFlag)
            Log.d(TAG, tag + ": " + content);       
    }
    
    public static final void w(String tag, String content){
        if(wFlag)
            Log.w(TAG, tag + ": " + content);    
    }
    
    public static final void e(String tag, String content){
        if(eFlag)
            Log.e(TAG, tag + ": " + content);
    }
    
    public static final void v(String tag, String content){
        if(vFlag)
            Log.v(TAG, tag + ": " + content);
    }
}
