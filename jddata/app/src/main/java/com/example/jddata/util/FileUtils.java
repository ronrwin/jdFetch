package com.example.jddata.util;


import android.content.Context;
import android.text.TextUtils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

public class FileUtils {
    /**
     * write mode, only for RandomAccessFile
     */
    public static final byte WRITE_POS_CURRENT_POS = 0;
    public static final byte WRITE_POS_BEGIN = 1;
    public static final byte WRITE_POS_END = 2;
    public static final byte WRITE_POS_SPECIFIED = 3;

    public static void writeToFile(String folder, String fileName, String content, boolean append) {
        writeToFile(folder, fileName, content, append, "UTF-8");
    }

    public static void writeToFile(String folder, String fileName, String content, boolean append, String encode) {
        try {
            File file = new File(folder);
            if (!file.exists()) {
                file.mkdirs();
            }

            String path = file.getAbsolutePath() + File.separator + fileName;
            File targetFile = new File(path);
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + File.separator + fileName, append);
            OutputStreamWriter osw = new OutputStreamWriter(fos, encode);
            osw.write(content);
            osw.flush();
            osw.close();
        } catch (Exception e) {
            System.out.println(" write file error!!");
            e.printStackTrace();
        }
    }

    public static String readFromAssets(Context context, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        try {
            InputStreamReader inputReader = new InputStreamReader( context.getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            StringBuilder Result=new StringBuilder();
            while((line = bufReader.readLine()) != null)
                Result.append(line+"\n");
            return Result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 拷贝assets文件下文件到指定路径
     *
     * @param context
     * @param assetDir  源文件/文件夹
     * @param targetDir  目标文件夹
     * @throws Exception
     */
    public static void copyAssets(Context context, String assetDir, String targetDir) throws Exception{
        if (TextUtils.isEmpty(assetDir) || TextUtils.isEmpty(targetDir)) {
            return;
        }
        String separator = File.separator;
        // 获取assets目录assetDir下一级所有文件以及文件夹
        String[] fileNames = context.getResources().getAssets().list(assetDir);
        // 如果是文件夹(目录),则继续递归遍历
        if (fileNames.length > 0) {
            File targetFile = new File(targetDir);
            if (!targetFile.exists() && !targetFile.mkdirs()) {
                return;
            }
            for (String fileName : fileNames) {
                copyAssets(context, assetDir + separator + fileName, targetDir + separator + fileName);
            }
        } else { // 文件,则执行拷贝
            copy(context, assetDir, targetDir);
        }
    }
    /**
     * 复制文件
     *
     * @param context 上下文对象
     * @param zipPath 源文件
     * @param targetPath 目标文件
     * @throws Exception
     */
    public static void copy(Context context, String zipPath, String targetPath) throws Exception{
        if (TextUtils.isEmpty(zipPath) || TextUtils.isEmpty(targetPath)) {
            return;
        }
        Exception exception = null;
        File dest = new File(targetPath);
        dest.getParentFile().mkdirs();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(context.getAssets().open(zipPath));
            out = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            exception = new Exception(e);
        } catch (IOException e) {
            exception = new Exception(e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                exception = new Exception(e);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }



    public static void writeToFile(String folder, String fileName, String content) {
        writeToFile(folder, fileName, content, false);
    }

    public static byte[] readBytes(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        return readBytes(new File(filePath));
    }

    public static byte[] readBytes(File file) {
        FileInputStream fileInput = null;
        try {
            if (file.exists()) {
                fileInput = new FileInputStream(file);
                return readFullBytes(fileInput);
            }
        } catch (Exception e) {
        } finally {
            safeClose(fileInput);
        }
        return null;
    }

    public static byte[] readFullBytes(InputStream input) {
        if (input == null)
            return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);

        try {
            byte[] buffer = new byte[2048];
            int offset;
            while ((offset = input.read(buffer, 0, buffer.length)) >= 0) {
                baos.write(buffer, 0, offset);
            }

            byte[] data = baos.toByteArray();
            return data;
        } catch (Exception e) {
        } finally {
            safeClose(baos);
        }

        return null;

    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    public static boolean writeBytes(String filePath, String fileName, byte[] data) {
        if (data == null) {
            return false;
        }
        return writeBytes(filePath, fileName, data, 0, data.length);
    }

    public static boolean writeBytes(String filePath, String fileName, byte[] data, int offset, int len) {
        try {
            return writeBytes(filePath, fileName, null, data, offset, len, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeBytes(String filePath, String fileName, byte[] headData, byte[] bodyData,
                                     int bodyOffset, int bodyLen, boolean forceFlush) throws FileNotFoundException, IOException {
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(fileName) || bodyData == null) {
            return false;
        }

        String tempFileName = System.currentTimeMillis() + fileName;

        File tempFile = createNewFile(filePath + tempFileName);

        boolean result = writeBytesBase(tempFile, headData, bodyData, bodyOffset, bodyLen, forceFlush);
        if (!result) {
            return false;
        }

        String srcPath = filePath + fileName;
        if (!rename(tempFile, srcPath)) {
            // rename srcPath到bakPath后再 delete bakPath，替代直接 delete srcPath
            String bakPath = genBackupFilePath(srcPath);
            delete(bakPath);
            rename(new File(srcPath), bakPath);

            result = rename(tempFile, srcPath);
            if (!result) {
                return false;
            }

            delete(bakPath);
        }

        return true;
    }

    public static boolean writeBytes(String path, byte mode, int specifiedPos, byte[] data) {
        RandomAccessFile raf = openFile(path, false);
        if (null == raf) {
            return false;
        }

        boolean ret = writeBytes(raf, mode, specifiedPos, data);
        safeClose(raf);

        return ret;
    }

    public static boolean writeBytes(RandomAccessFile raf, byte mode, int specifiedPos, byte[] data) {
        if (null == raf || data == null || data.length == 0) {
            return false;
        }

        try {
            switch (mode) {
                case WRITE_POS_CURRENT_POS:
                    break;
                case WRITE_POS_BEGIN:
                    raf.seek(0);
                    break;
                case WRITE_POS_END: {
                    long len = raf.length();
                    raf.seek(len);
                    break;
                }
                case WRITE_POS_SPECIFIED: {
                    raf.seek(specifiedPos);
                    break;
                }
                default:
                    break;
            }

            raf.write(data);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean writeBytes(File file, byte[] data, int offset, int len) {
        return writeBytes(file, null, data, offset, len, false);
    }

    public static boolean writeBytes(File file, byte[] headData, byte[] bodyData, int bodyOffset, int bodyLen, boolean forceFlush) {
        try {
            return writeBytesBase(file, headData, bodyData, bodyOffset, bodyLen, forceFlush);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeBytesBase(File file, byte[] headData, byte[] bodyData, int bodyOffset, int bodyLen,
                                         boolean forceFlush) throws FileNotFoundException, IOException {
        FileOutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream(file);
            if (headData != null) {
                fileOutput.write(headData);
            }
            fileOutput.write(bodyData, bodyOffset, bodyLen);
            fileOutput.flush();
            if (forceFlush) {
                FileDescriptor fd = fileOutput.getFD();
                if (fd != null) {
                    fd.sync(); // 立刻刷新，保证文件可以正常写入;
                }
            }
            return true;
        } finally {
            safeClose(fileOutput);
        }
    }

    public static File createNewFile(String path) {
        return createNewFile(path, false);
    }

    public static File createNewFile(String path, boolean append) {
        File newFile = new File(path);
        if (!append) {
            if (newFile.exists()) {
                newFile.delete();
            }
        }
        if (!newFile.exists()) {
            try {
                File parent = newFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                newFile.createNewFile();
            } catch (Exception e) {
                // #if (debug == true)
                e.printStackTrace();
                // #endif
            }
        }
        return newFile;
    }

    public static RandomAccessFile openFile(String path, boolean append) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path, "rw");
            if (append) {
                long len = file.length();
                if (len > 0) {
                    file.seek(len);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return file;
    }

    public static boolean rename(File file, String newName) {
        return file.renameTo(new File(newName));
    }

    public static boolean delete(String path) {
        return delete(new File(path));
    }

    public static boolean delete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean success = delete(new File(file, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        boolean suc = false;
        try {
            // The directory is now empty so delete it
            suc = file.delete();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return suc;
    }

    public final static String EXT_BAK = ".bak";

    public static String genBackupFilePath(String filePath) {
        return filePath + EXT_BAK;
    }
}
