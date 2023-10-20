package com.ma.enterprisemodepolicymanager.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ShellUtils {
    private static final String TAG = "ShellUtils";
    private ShellUtils() {
    }
    public static String execCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh"});

            OutputStream outputStream = process.getOutputStream();
            outputStream.write((command + "\n").getBytes());
            outputStream.flush();
            outputStream.close();

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            outputThread.start();
            errorThread.start();

            int exitCode = process.waitFor();
            outputThread.join();
            errorThread.join();

            if (exitCode != 0) {
                return error.toString();
            }

            return output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
