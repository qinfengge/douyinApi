package xyz.qinfengge.douyinapi.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.qinfengge.douyinapi.config.SystemConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lizhiao
 * @date 2022/9/9 14:27
 */
@Component
public class FFmpegUtil {

    @Autowired
    private SystemConfig systemConfig;

    public String getScreenShot(String videoResourcesPath, String picPath) {
        String ffmpeg = systemConfig.getFfmpegPath();
        System.out.println(ffmpeg);
        /**
         * 保存图片截图的文件夹
         */
        String picturMediaPath = picPath;
        if (!new File(picturMediaPath).exists()){
            new File(picturMediaPath).mkdirs();
        }
        //时间作为截图后的视频名
        String getdatatime = nowTime();
        List<String> command = new ArrayList<>();
        command.add(ffmpeg);
        command.add("-ss");
        command.add("3");
        command.add("-i");
        command.add(videoResourcesPath);
        command.add("-f");
        command.add("image2");
        String fileName = videoResourcesPath.substring(videoResourcesPath.lastIndexOf("\\") + 1, videoResourcesPath.lastIndexOf("."));
        command.add(picturMediaPath + fileName + getdatatime + ".jpg");
        commandStart(command);
        return fileName + getdatatime + ".jpg";
    }

    /**
     * 调用命令行执行
     *
     * @param command 命令行参数
     */
    public static void commandStart(List<String> command) {
        command.forEach(v -> System.out.print(v + " "));
        System.out.println();
        System.out.println();
        ProcessBuilder builder = new ProcessBuilder();
        //正常信息和错误信息合并输出
        builder.redirectErrorStream(true);
        builder.command(command);
        //开始执行命令
        Process process = null;
        try {
            process = builder.start();
            //如果你想获取到执行完后的信息，那么下面的代码也是需要的
            String line = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = br.readLine()) != null) {
//				System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前时间，用于作为文件名
     */
    public static String nowTime() {
        DateTimeFormatter f3 = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        LocalDate nowdata = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        return nowdata.atTime(nowTime).format(f3);
    }

}
