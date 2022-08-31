package xyz.qinfengge.douyinapi.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.qinfengge.douyinapi.config.SystemConfig;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.result.Result;
import xyz.qinfengge.douyinapi.service.VideoService;
import xyz.qinfengge.douyinapi.mapper.VideoMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.UUID;
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

    String path;
    String prePath;

    @Override
    public Result init(String isRename) throws IOException {
        Boolean b = "true".equals(isRename);
        System.out.println(systemConfig.getFileInputDir());
        handleFile(systemConfig.getFileInputDir(), b);
        return Result.ok("init成功！");
    }

    private void add(String name, String prePath) throws UnsupportedEncodingException {
        //上传到的服务器路径 用于拼接视频URL
        String ip = systemConfig.getSiteUrl();
        Video video = new Video();
        video.setName(name);
        String encode = URLEncoder.encode(name, "UTF-8");
        video.setUrl(ip + "/" + prePath + "/" + encode);
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
                } else if (b) {
                    doRename(f);
                    handleFile(f.getAbsolutePath(), b);
                } else {
                    add(f.getName(), prePath);
                }
            } else if (f.isDirectory()) {
                prePath = f.getName();
                path = f.getAbsolutePath();
                handleFile(f.getAbsolutePath(), b);
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
        add(newName, prePath);
    }
}
