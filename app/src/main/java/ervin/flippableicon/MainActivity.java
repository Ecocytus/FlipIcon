package ervin.flippableicon;

import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements FlippableIcon.ViewCallback {

    FlippableIcon icon1;
    FlippableIcon icon2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        icon1 = findViewById(R.id.icon1);
        icon2 = findViewById(R.id.icon2);
    }

    /**
     * if you want to change the icons to other pictures/text/etc
     * just design your own {@link FlippableIcon#onDraw(Canvas)}}
     * It is a simple onDraw, only contains two icons
     */
    void changePic() { }

    @Override
    public void onTouch() {}

    @Override
    protected void onResume() {
        super.onResume();
        FlippableIcon.setCallback(this);
        // null means no change
        // default is 2.5, 40
        icon1.setConfig(2.0, 20.0);
        icon1.setDoubleClickBound(2);


    }

    @Override
    protected void onPause() {
        super.onPause();
        FlippableIcon.removeCallback();
    }
}
