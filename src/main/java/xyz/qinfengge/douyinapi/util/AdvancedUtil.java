package xyz.qinfengge.douyinapi.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lza
 * @date 2023/10/12-17:33
 **/


@Component
public class AdvancedUtil {

    @Resource
    private FileUtils fileUtils;

    @SneakyThrows
    public void walkFileTree(String path){
        AtomicInteger dircount = new AtomicInteger();
        AtomicInteger filecount = new AtomicInteger();
        Files.walkFileTree(Paths.get(path),new SimpleFileVisitor<Path>(){
            //进入文件夹触发
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                System.out.println("=================="+dir);
                dircount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }
            //进入文件触发
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                filecount.incrementAndGet();
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Map<String, Object> map = fileUtils.rename(dir.getFileName().toString());
                System.out.println("文件名:" + map.get("fileName"));
                System.out.println("标签:" + map.get("tags"));
                System.out.println("创建日期:" + map.get("created"));

                List<String> list = FileUtil.listFileNames(dir.toString());

                // 当文件夹中有文件才更新文件夹名，需要上传文件夹内以账号命名的txt文件
                if (!list.isEmpty()){
                    FileUtil.rename(dir, map.get("fileName").toString(), false);
                }
                return super.postVisitDirectory(dir, exc);
            }
        });
        System.out.println("文件夹数量："+dircount+"文件数量："+filecount);
    }
}
