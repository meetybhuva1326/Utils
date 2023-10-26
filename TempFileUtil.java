package com.example.pdftoimage.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

public class TempFileUtil {

    static Context mainContext;

    public static File from(Context context, Uri uri) throws IOException {
        mainContext=context;
        File file;
        InputStream openInputStream = context.getContentResolver().openInputStream(uri);
        String fileName = getFileName(uri);
        String[] splitFileName = splitFileName(fileName);
        try {
            file = File.createTempFile(splitFileName[0], splitFileName[1]);
        } catch (Exception unused) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            file = File.createTempFile(timestamp.getTime() + "", splitFileName[1]);
        }
        File rename = rename(file, fileName);
        rename.deleteOnExit();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(rename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (openInputStream != null) {
            copy(openInputStream, fileOutputStream);
            openInputStream.close();
        }
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
        return rename;
    }

    private static void copy(InputStream openInputStream, FileOutputStream fileOutputStream) throws IOException {
        byte[] buf = new byte[1024];
        int len;

        while ((len = openInputStream.read(buf)) > 0) {
            fileOutputStream.write(buf, 0, len);
        }
    }


    @SuppressLint("Range")
    public static String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = mainContext.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static String[] splitFileName(String str) {
        String str2;
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf != -1) {
            String substring = str.substring(0, lastIndexOf);
            str2 = str.substring(lastIndexOf);
            str = substring;
        } else {
            str2 = "";
        }
        return new String[]{str, str2};
    }

    private static File rename(File file, String str) {
        File file2 = new File(file.getParent(), str);
        if (!file2.equals(file)) {
            if (file2.exists() && file2.delete()) {
                Log.d("FileUtil", "Delete old " + str + " file");
            }
            if (file.renameTo(file2)) {
                Log.d("FileUtil", "Rename file to " + str);
            }
        }
        return file2;
    }

    public static void delete(String str) {
        File file = new File(str);
        file.delete();
    }

}
