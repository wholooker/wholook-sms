package net.wholook.wmessage.service;

import android.app.Application;
import android.util.Log;
import android.content.res.Configuration;

import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import net.wholook.wmessage.api.WholookAPI;

/**
 * Created by wholook on 14. 9. 22..
 */
public class WMessageApplication extends Application {

    private UncaughtExceptionHandler mUncaughtExceptionHandler;

    public void onCreate() {
        // First, call the parent class


        //mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        //Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerApplication());

        super.onCreate();

        // This is a good place to put code that must manage global data across
        // multiple activities, but it's better to keep most things in a
        // database, rather than in memory
        Log.d(WholookAPI.LOG_TAG, "-------------WMessageApplication - onCreate()-------------");
    }

    @Override
    public void onTerminate() {
        Log.d(WholookAPI.LOG_TAG, "-------------WMessageApplication - onTerminate()-------------");
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        Log.d(WholookAPI.LOG_TAG, "-------------WMessageApplication - onConfigurationChanged()-------------");
    }
    @Override
    public void onLowMemory(){
        Log.d(WholookAPI.LOG_TAG, "-------------WMessageApplication - onLowMemory()-------------");
    }

    private String getStackTrace(Throwable th) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }

    class UncaughtExceptionHandlerApplication implements Thread.UncaughtExceptionHandler{
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            //예외상황이 발행 되는 경우 작업

            Log.d(WholookAPI.LOG_TAG, "-------------WMessageApplication - UncaughtExceptionHandlerApplication()-------------");
            Log.d(WholookAPI.LOG_TAG, "error - " + getStackTrace(ex));
            //예외처리를 하지 않고 DefaultUncaughtException으로 넘긴다.
            mUncaughtExceptionHandler.uncaughtException(thread, ex);

        }
    }
}
