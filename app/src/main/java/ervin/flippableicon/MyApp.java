package ervin.flippableicon;

import android.app.Application;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            Log.d("ERVIN", "Analyze");
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        Log.d("ERVIN", "START APP");
        // Normal app init code...*/
    }
}