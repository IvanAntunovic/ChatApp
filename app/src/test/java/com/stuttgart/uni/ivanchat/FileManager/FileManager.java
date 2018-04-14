package com.stuttgart.uni.ivanchat.FileManager;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;

public class FileManager {

    public static void store(Context context) {

        String filename = "Ivan_Chat";
        String fileContents = "Hello world!";
        FileOutputStream outputStream;

        try {

            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}
