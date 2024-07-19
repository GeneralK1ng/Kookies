package org.kookies.mirai.commen.utils;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.kookies.mirai.commen.constant.MsgConstant;
import org.kookies.mirai.commen.exceptions.ThumbnailTakeException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author General_K1ng
 */
public class VideoThumbTaker {
    private static final String FILE_EXT = "jpg";

    private static final int THUMB_FRAME = 5;

    /**
     * 从视频文件中提取帧图像。
     * <p>
     * 此方法通过指定的视频文件路径和帧文件路径，提取视频中的某一帧作为图像保存。
     * 主要用于视频预览或视频关键帧的提取与保存。
     *
     * @param videoFilePath 视频文件的路径，用于指定要提取帧的视频文件。
     * @param frameFilePath 帧图像的保存路径，指定提取的帧将保存到的位置。
     * @throws IOException 如果在读取视频文件或保存帧图像时发生I/O错误，将抛出此异常。
     */
    public static void fetchFrame(String videoFilePath, String frameFilePath) throws IOException {
        File videoFile = new File(videoFilePath);
        File frameFile = new File(frameFilePath);
        generateThumbnail(videoFile, frameFile);
    }

    /**
     * 根据视频文件生成缩略图。
     * <p>
     * 该方法通过解析视频文件中的指定帧，生成缩略图图像并保存到指定的文件中。
     *
     * @param videoFile 视频文件，用于生成缩略图的源文件。
     * @param frameFile 缩略图文件，保存生成的缩略图的目标文件。
     * @throws ThumbnailTakeException 如果生成缩略图过程中发生错误，将抛出此异常。
     */
    private static void generateThumbnail(File videoFile, File frameFile) {
        try {
            // 从视频文件中提取指定帧
            Picture picture = FrameGrab.getFrameFromFile(videoFile, THUMB_FRAME);
            // 将提取的帧转换为BufferedImage对象
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
            // 将转换后的图像写入到指定的文件中
            ImageIO.write(bufferedImage, FILE_EXT, frameFile);
        } catch (IOException | JCodecException e) {
            // 如果在生成缩略图过程中发生IO异常或JCodec异常，则抛出自定义的ThumbnailTakeException异常
            throw new ThumbnailTakeException(MsgConstant.VIDEO_THUMB_TAKE_ERROR);
        }
    }



}
