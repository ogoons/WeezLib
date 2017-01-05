package com.ogoons.weezlibsample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.ogoons.weezlib.WeezButton;
import com.ogoons.weezlib.WeezRollBanner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private WeezButton      mButton;
    private WeezRollBanner  mRollBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (WeezButton) findViewById(R.id.button);
        mRollBanner = (WeezRollBanner) findViewById(R.id.rollbanner);

        mButton.setImageResource(R.mipmap.ic_launcher);

        ArrayList images = new ArrayList();
        images.add("http://i.imgur.com/dPzSuNT.png");
        images.add("http://i.imgur.com/BS7rc0R.jpg");
        images.add("http://i.imgur.com/E69U5lP.png");
        images.add("http://i.imgur.com/rsEyWZv.png");

        // Android/data/com.ogoons.weezlibsample/files/Download
        String incomingPath = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        mRollBanner.startRolling(images, incomingPath, 3000);
    }
}
