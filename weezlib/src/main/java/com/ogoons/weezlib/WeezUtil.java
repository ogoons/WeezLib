package com.ogoons.weezlib;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by OGOONS on 2016-09-25.
 */
public class WeezUtil {
    public static class Widget {
        private static ProgressDialog mProgressDialog;

        public static void toast(Context context, String text) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }

        public static void toast(Context context, int resId) {
            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
        }

        public static void showProgressDialog(Context context, String message) {
            if (mProgressDialog == null)
                mProgressDialog = new ProgressDialog(context);

            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        public static void showProgressDialog(Context context, int stringResId) {
            if (mProgressDialog == null)
                mProgressDialog = new ProgressDialog(context);

            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            if (mProgressDialog.getWindow() != null) {
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage(context.getString(stringResId));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
        }

        public static void hideProgressDialog() {
            if (mProgressDialog != null && mProgressDialog.isShowing() && mProgressDialog.getWindow() != null) {
                try {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Size {
        public static float spToPx(Context ctx, int sp)
        {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, ctx.getResources().getDisplayMetrics());
        }

        public static int dpToPx(Context ctx, int dp)
        {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
        }

        public static int pxToDp(Context context, int pixel)
        {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float dp = pixel / (metrics.densityDpi / 160f);

            return (int) dp;
        }
    }

    public static class Path {
        /**
         * 파일 경로에서 파일 이름을 추출한다.
         * @param path
         * @return
         */
        public static String getFileName(String path) {
            int lastIndexOfSlash = path.lastIndexOf("/");
            if (lastIndexOfSlash > 0)
                return path.substring(lastIndexOfSlash + 1, path.length());
            else
                return path;
        }

        /**
         * 파일 경로에서 확장자를 제외한 파일 이름을 추출한다.
         * @param path
         * @return
         */
        public static String getPureFileName(String path) {
            String fileName = getFileName(path);
            int lastIndexOfDot = fileName.lastIndexOf(".");
            if (lastIndexOfDot > 0) {
                return fileName.substring(0, lastIndexOfDot);
            } else {
                return "";
            }
        }

        /**
         * URL에서 확장자를 포함한 파일 이름을 추출한다.
         * @param url
         * @return
         * @warning url만 인자로 추가하도록 한다.
         */
        public static String getFileNameFromWebUrl(String url) {
            String rawFileName = url;
            if (rawFileName.lastIndexOf("/") > 0) { // 하위 경로가 있는 url 형태이면
                rawFileName = rawFileName.substring(rawFileName.lastIndexOf("/") + 1);
                if (rawFileName.lastIndexOf("?") > 0) { // 파일 뒤에 파라미터가 있는 형태이면 잘라낸다.
                    rawFileName = rawFileName.substring(0, rawFileName.lastIndexOf("?"));
                }
            }

            return rawFileName;
        }

        /**
         * 파일 경로로부터 파일 확장자 추출
         * @param path
         * @return
         */
        public static String getFileExt(String path) {
            int lastIndexOfDot = path.lastIndexOf(".");
            if (lastIndexOfDot > 0)
                return path.substring(lastIndexOfDot + 1, path.length());
            else
                return "";
        }

        /**
         * 파일 경로로부터 파일명을 제외한 경로 호출
         * @param path
         * @return
         */
        public static String getPath(String path) {
            int lastIndexOfSlash = path.lastIndexOf("/");
            if (lastIndexOfSlash > 0)
                return path.substring(0, lastIndexOfSlash - 1);
            else
                return "";
        }

        /**
         * 디렉토리 트리 모두 삭제 (재귀호출)
         * @param path
         * @return
         */
        public static boolean deleteDirs(String path) {
            File file = new File(path);
            File[] childFileList = file.listFiles();
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    deleteDirs(childFile.getAbsolutePath());     //하위 디렉토리 루프
                } else {
                    childFile.delete();    //하위 파일삭제
                }
            }
            return file.delete();    //root 삭제
        }
    }
    
    public static class Time {
        public static String getCurrentTime(String format) {
            long time = System.currentTimeMillis();
            if (TextUtils.isEmpty(format)) {
                format = "yyyyMMddHHmmssSSS";
            }
            SimpleDateFormat dayTime = new SimpleDateFormat(format);
            return dayTime.format(new Date(time));
        }
    }

    public static class Network {
        public static boolean downloadFile(final String fileUrl, final String incomingPath) {
            try {
                final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
                String encodedUrl = Uri.encode(fileUrl, ALLOWED_URI_CHARS);
                URL url = new URL(encodedUrl);
                File file = new File(incomingPath);

                //서버와 접속하는 클라이언트 객체 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int len = conn.getContentLength();
                byte[] fileBytes = new byte[len];

                // 입력 스트림을 구한다
                InputStream is = conn.getInputStream();

                // 파일 저장 스트림 생성
                FileOutputStream fos = new FileOutputStream(file);

                int read = 0;

                // 입력 스트림을 파일로 저장
                while (true) {
                    read = is.read(fileBytes);
                    if (read <= 0) {
                        break;
                    }
                    fos.write(fileBytes, 0, read); //file 생성
                }

                is.close();
                fos.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }
}
