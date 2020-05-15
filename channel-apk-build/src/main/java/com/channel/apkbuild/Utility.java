package com.channel.apkbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 工具类
 */
public class Utility {

    public static void copyFile(String fromPath, String toPath) throws IOException {
        File fromF = new File(fromPath);
        File toF = new File(toPath);

        copyFile(fromF, toF);
    }

    public static void copyFile(File fromFile, File toFile) throws IOException {
        if (!fromFile.exists()) {
            return;
        }
        if (!toFile.exists()) {
            if (fromFile.isDirectory() && !toFile.mkdirs()) {
                throw new RuntimeException("can't create dir, path:" + toFile);
            }
        }

        if (fromFile.isFile()) {
            copyFileInner(fromFile, toFile);
        } else if (fromFile.isDirectory()) {
            File[] listFiles = fromFile.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    String name = file.getName();
                    if (file.isFile()) {
                        copyFileInner(file, new File(toFile, name));
                    } else if (file.isDirectory()) {
                        copyFile(new File(fromFile, name), new File(toFile, name));
                    }
                }
            }
        }
    }

    private static void copyFileInner(File f1, File f2) throws IOException {
        FileChannel from = new FileInputStream(f1).getChannel();

        File toDir = f2.getParentFile();
        if (!toDir.exists() || !toDir.isDirectory()) {
            toDir.mkdirs();
        }
        FileChannel to = new FileOutputStream(f2, false).getChannel();
        to.transferFrom(from, 0, from.size());
        from.close();
        to.close();
    }

    public static String format(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT)).format(date);
    }

}
