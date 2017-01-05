package com.ogoons.weezlib;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by ogoons on 2016-08-17.
 */
public class WeezRollBanner extends LinearLayout  {
    public static final String TAG = WeezRollBanner.class.getSimpleName();

    private ImageSwitcher       mImageSwitcher;
    private ArrayList<String>   mImagePaths;
    private int                 mDelay = 0;
    private int                 mPos = 0;
    private boolean             mIsRunning = false;
    private String              mIncomingPath;
    private RollingHandler      mRollingHandler;

    public WeezRollBanner(Context context) {
        super(context);
        initialize(context);
    }

    public WeezRollBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public WeezRollBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.layout_roll_banner, this, true);

        mImagePaths = new ArrayList<String>();
        mRollingHandler = new RollingHandler();

        mImageSwitcher = (ImageSwitcher) findViewById(R.id.is_ad);
        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView iv = new ImageView(getContext());
                iv.setBackgroundColor(Color.WHITE);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                iv.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

                // Ripple effect 추가
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                iv.setBackgroundResource(outValue.resourceId);

                getContext().getTheme().resolveAttribute(android.R.attr.clickable, outValue, true);
                iv.setBackgroundResource(outValue.resourceId);

                return iv;
            }
        });
    }

    // 이미지 보여주기 시간 설정
    public void setDelayTime(int milliSec) {
        mDelay = milliSec;
    }

    protected void setImages(ArrayList<String> imagePaths) {
        mImagePaths = (ArrayList<String>) imagePaths.clone();
    }

    protected void setIncomingPath(String incomingPath) {
        mIncomingPath = incomingPath;
    }

    // 이미지 모두 제거 & 롤링 중지
    public void clearImage() {
        stopRolling();
        mImagePaths.clear();
    }

    private class RollingRunnable implements Runnable {
        public RollingRunnable() {}

        @Override
        public void run() {
            try {
                while (mIsRunning) {
                    Message msg = mRollingHandler.obtainMessage();
                    msg.what = 0;
                    msg.obj = mImagePaths.get(mPos++);

                    if (mImagePaths.size() == mPos) {
                        mPos = 0;
                    }

                    mRollingHandler.sendMessage(msg);

                    if (1 == mImagePaths.size()) { // 한 번만 실행
                        break;
                    }

                    Thread.sleep(mDelay);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class RollingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (0 == msg.what) {
                String imagePath = (String) msg.obj;

                File file = new File(imagePath);
                if (file.exists()) {
                    String path = "file:///" + imagePath;
                    Uri imageUri = Uri.parse(path);
                    mImageSwitcher.setImageURI(imageUri);
                }
            }
        }
    }

    private class LoadImagesTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            ArrayList<String> imagePaths = params[0];

            for (int i = 0; i < imagePaths.size(); i++) {
                String imagePath = imagePaths.get(i);

                // 로컬 이미지일 경우 건너뛴다.
                Uri uri = Uri.parse(imagePath);
                String protocol = uri.getScheme();
                if (!protocol.equals("http") && !protocol.equals("https")) {
                    continue;
                }

                // 폴더 생성
                File tempDir = new File(mIncomingPath);
                if (!tempDir.exists()) {
                    if (!tempDir.mkdirs()) {
                        Log.d(WeezRollBanner.TAG, "Create directory failure");
                    }
                }

                String rawFileName = imagePath;
                if (rawFileName.lastIndexOf("/") > 0) {
                    rawFileName = rawFileName.substring(rawFileName.lastIndexOf("/") + 1);
                    if (rawFileName.lastIndexOf("?") > 0) {
                        rawFileName = rawFileName.substring(0, rawFileName.lastIndexOf("?"));
                    }
                }

                // 로컬에 이미지 파일 존재 여부 확인
                String incomingPath = mIncomingPath + "/" + rawFileName;
                File imageFile = new File(incomingPath);
                if (!imageFile.exists()) {
                    // 이미지 다운로드
                    WeezUtil.Network.downloadFile(imagePath, incomingPath);
                }

                imagePaths.set(i, incomingPath);
            }

            return imagePaths;
        }

        @Override
        protected void onPostExecute(ArrayList<String> imagePaths) {
            super.onPostExecute(imagePaths);

            mImagePaths = imagePaths;

            // 롤링 시작
            RollingRunnable rollingRunnable = new RollingRunnable();
            Thread thread = new Thread(rollingRunnable);
            thread.start();
        }
    }

    // 이미지 전환 시작
    public boolean startRolling(ArrayList<String> imagePaths, String incomingPath, int delay) {
        if (0 == delay || imagePaths.isEmpty()) {
            return false;
        }

        mIsRunning = true;
        setImages(imagePaths);
        setIncomingPath(incomingPath);
        setDelayTime(delay);

        new LoadImagesTask().execute(imagePaths);

        return true;
    }

    // 중지
    public void stopRolling() {
        mPos = 0;
        mIsRunning = false;
    }
}