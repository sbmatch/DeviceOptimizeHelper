package com.sbmatch.deviceopt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FilesUtils {

    /**
     * 创建文件
     * @param filePath 文件路径
     * @return 创建成功返回true，否则返回false
     */
    public static boolean createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return false; // 文件已存在，创建失败
        }
        try {
            return file.createNewFile(); // 创建文件
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建文件夹
     * @param dirPath 文件夹路径
     * @return 创建成功返回true，否则返回false
     */
    public static boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            return false; // 文件夹已存在，创建失败
        }
        return dir.mkdirs(); // 创建文件夹
    }

    /**
     * 删除文件或文件夹
     * @param path 文件或文件夹路径
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false; // 文件或文件夹不存在，删除失败
        }
        if (file.isDirectory()) {
            // 如果是文件夹，则递归删除文件夹下的所有文件和子文件夹
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    delete(child.getAbsolutePath());
                }
            }
        }
        return file.delete(); // 删除文件或文件夹
    }

    /**
     * 移动文件或文件夹
     * @param srcPath 源文件或文件夹路径
     * @param destPath 目标文件或文件夹路径
     * @return 移动成功返回true，否则返回false
     */
    public static boolean move(String srcPath, String destPath) {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        if (!srcFile.exists()) {
            return false; // 源文件或文件夹不存在，移动失败
        }
        return srcFile.renameTo(destFile); // 移动文件或文件夹
    }

    /**
     * 判断文件是否存在
     * @param filePath 文件路径
     * @return 存在返回true，否则返回false
     */
    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    /**
     * 判断文件夹是否存在
     * @param dirPath 文件夹路径
     * @return 存在返回true，否则返回false
     */
    public static boolean isDirExists(String dirPath) {
        File dir = new File(dirPath);
        return dir.exists() && dir.isDirectory();
    }

    /**
     * 向文件写入内容
     * @param filePath 文件路径
     * @param content 写入的内容
     * @return 写入成功返回true，否则返回false
     */
    public static boolean writeToFile(String filePath, String content, boolean isAppend) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filePath,isAppend));
            writer.write(content);
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从文件中读取内容
     * @param filePath 文件路径
     * @return 读取到的内容，如果读取失败返回null
     */
    public static String readFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null; // 文件不存在或不是文件，读取失败
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File getLatestFileInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        // 获取目录中的所有文件
        File[] files = directory.listFiles();

        // 如果目录为空，返回null
        if (files == null || files.length == 0) {
            return null;
        }

        // 使用文件的最后修改时间进行排序
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                long lastModified1 = file1.lastModified();
                long lastModified2 = file2.lastModified();
                return Long.compare(lastModified2, lastModified1); // 降序排列
            }
        });

        // 返回最新的文件
        return files[0];
    }

    public static File getLatestFileInDirectoryWithPrefix(String directoryPath, String prefix) {
        File directory = new File(directoryPath);

        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        // 获取目录中的所有文件
        File[] files = directory.listFiles();

        // 如果目录为空，返回null
        if (files == null || files.length == 0) {
            return null;
        }

        // 过滤出以指定前缀开头的文件
        List<File> matchingFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(prefix)) {
                matchingFiles.add(file);
            }
        }

        // 如果没有匹配的文件，返回null
        if (matchingFiles.isEmpty()) {
            return null;
        }

        // 使用文件的最后修改时间进行排序
        matchingFiles.sort(new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                long lastModified1 = file1.lastModified();
                long lastModified2 = file2.lastModified();
                return Long.compare(lastModified2, lastModified1); // 降序排列
            }
        });

        // 返回最新的匹配文件
        return matchingFiles.get(0);
    }


}
