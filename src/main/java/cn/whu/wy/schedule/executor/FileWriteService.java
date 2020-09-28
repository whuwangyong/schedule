package cn.whu.wy.schedule.executor;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @Author WangYong
 * @Date 2020/05/31
 * @Time 23:01
 */
@Component
public class FileWriteService {

    public void write(String path, String str) {
        File file = new File(path);
        try {
            FileWriter fw = new FileWriter(file, file.exists());
            String sb = getTimestamp() + " --- " + str + "\n";
            fw.write(sb);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTimestamp() {
        return LocalDateTime.now().toString().replace('T', ' ');
    }

    //Test
    public static void main(String[] args) {
        FileWriteService writeService = new FileWriteService();
        writeService.write("C:\\Users\\wangyong\\Desktop\\adssadas.txt", "test1");
        writeService.write("C:\\Users\\wangyong\\Desktop\\adssadas.txt", "test2");
        writeService.write("C:\\Users\\wangyong\\Desktop\\adssadas.txt", "test3");
    }
}
