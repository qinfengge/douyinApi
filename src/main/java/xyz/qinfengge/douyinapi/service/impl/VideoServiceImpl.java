package xyz.qinfengge.douyinapi.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.qinfengge.douyinapi.config.SystemConfig;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.result.Result;
import xyz.qinfengge.douyinapi.service.VideoService;
import xyz.qinfengge.douyinapi.mapper.VideoMapper;
import org.springframework.stereotype.Service;
import xyz.qinfengge.douyinapi.util.FFmpegUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yasuo
 */
@Service("videoService")
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
        implements VideoService {

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private FFmpegUtil ffmpegUtil;

    /**
     * 当前所在目录路径
     */
    String path;

    /**
     * 当前目录名，不包含盘符
     */
    String prePath;

    /**
     * 标记，用来判断是否已有thumbnail文件夹
     * 如果已有thumbnail文件夹，则不会执行添加操作
     */
    Boolean flag = true;

    @Override
    public Result<Object> init(String isRename) throws IOException {
        Boolean b = "true".equals(isRename);
        handleFile(systemConfig.getFileInputDir(), b);
        if (flag){
            return Result.ok("init成功！");
        }else {
            return Result.fail("已经存在thumbnail文件夹！");
        }
    }

    private void add(String oldName, String newName, String prePath) throws UnsupportedEncodingException {
        //上传到的服务器路径 用于拼接视频URL
        String ip = systemConfig.getSiteUrl();
        String screenShot = ffmpegUtil.getScreenShot(path + "\\" + newName, systemConfig.getFileInputDir() + "\\" + "thumbnail" + "\\" + prePath + "\\");
        Video video = new Video();
        video.setName(oldName);
        String encode = URLEncoder.encode(newName, "UTF-8");
        String thumb = URLEncoder.encode(screenShot, "UTF-8");
        video.setUrl(ip + "/" + prePath + "/" + encode);
        video.setThumbnail(ip + "/" + "thumbnail" + "/" + prePath + "/" + thumb);
        this.getBaseMapper().insert(video);

    }

    private void handleFile(String fileDir, Boolean b) throws IOException {
        File file = new File(fileDir);
        // 获取目录下的所有文件或文件夹
        File[] files = file.listFiles();
        // 如果目录为空，直接退出
        if (files == null) {
            return;
        }
        // 遍历，目录下的所有文件
        for (File f : files) {
            if (f.isFile()) {
                if (f.length() == 0) {
                    f.delete();
                    System.err.println("已删除大小为0的空文件" + f.getName());
                } else if (b && flag) {
                    doRename(f);
                    handleFile(f.getAbsolutePath(), b);
                } else if (!b){
                    add(f.getName(), f.getName(), prePath);
                }else {
                    return;
                }
            } else if (f.isDirectory()) {
                prePath = f.getName();
                if ("thumbnail".equals(prePath)){
                    flag = false;
                    return;
                }else {
                    path = f.getAbsolutePath();
                    handleFile(f.getAbsolutePath(), b);
                }
            }
        }
    }

    private void doRename(File file) throws IOException {
        //使用正则去除文件名中的特殊符号
        //如果文件名中包含emoji，则可能失效
        String name = file.getName();
        String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\]<>/?￥%…（）_+|【】‘；：”“’。，、？\\s]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(name);
        String newName = m.replaceAll("");
        //重命名
        File dest = new File(path + "\\" + newName);
        file.renameTo(dest);
        add(name, newName, prePath);
    }
}
