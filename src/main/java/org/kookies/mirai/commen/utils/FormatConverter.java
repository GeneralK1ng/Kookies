package org.kookies.mirai.commen.utils;

import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.FormatConvertException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FormatConverter {

    public static byte[] convertWavToAmr(byte[] wavData) {
        try {
            // 将WAV数据写入临时文件
            Path tempWavFile = Files.createTempFile("temp", ".wav");
            try (OutputStream os = Files.newOutputStream(tempWavFile)) {
                os.write(wavData);
            }

            // 使用FFmpeg将WAV文件转换为AMR格式
            Path tempAmrFile = Files.createTempFile("temp", ".amr");
            ProcessBuilder processBuilder = new ProcessBuilder("/home/ffmpeg/target/bin/ffmpeg", "-y", "-i", tempWavFile.toString(), "-ar", "8000", "-ab", "12.2k", "-ac", "1", tempAmrFile.toString());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            System.out.println("Converting WAV to AMR...");
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // 进程执行失败，记录错误信息
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                }
                throw new FormatConvertException("Error converting WAV to AMR");
            }

            // 读取转换后的AMR文件数据
            byte[] amrData = Files.readAllBytes(tempAmrFile);

            // 删除临时文件
            Files.deleteIfExists(tempWavFile);
            Files.deleteIfExists(tempAmrFile);

            return amrData;
        } catch (IOException | InterruptedException e) {
            // 记录异常信息
            e.printStackTrace();
            throw new FormatConvertException("Error converting WAV to AMR: " + e.getMessage());
        }
    }
}
