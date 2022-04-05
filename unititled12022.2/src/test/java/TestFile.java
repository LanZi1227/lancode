import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestFile {
    public static void main(String[] args) throws IOException {
        String srcPath="d:/test1.txt";
        String descPath="d:/test2.txt";

        FileInputStream fileInputStream=new FileInputStream(srcPath);
        FileOutputStream fileOutputStream=new FileOutputStream(descPath);

        while (true){
            int ch=fileInputStream.read();
            if (ch==-1){
                break;
            }
            fileOutputStream.write(ch);
        }
        //如果不关闭，可能会导致文件资源泄露
        fileInputStream.close();
        fileOutputStream.close();
    }
}
