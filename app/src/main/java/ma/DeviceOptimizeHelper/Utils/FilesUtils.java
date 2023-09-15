package ma.DeviceOptimizeHelper.Utils;

import java.io.File;
import java.io.IOException;

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
}
