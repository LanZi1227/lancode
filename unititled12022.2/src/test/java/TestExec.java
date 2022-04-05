import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLOutput;

public class TestExec {
    public static void main(String[] args) throws IOException {
        Runtime runtime=Runtime.getRuntime();
        Process process=runtime.exec("javac" );
        //获取到子进程的的标准输出和标准错误，把这里的内容写入到两个文件中
        InputStream stdoutFrom=process.getInputStream();
        FileOutputStream stdoutTo=new FileOutputStream("stdout.txt");
        while (true){
            int ch=stdoutFrom.read();
            if (ch==-1){
                break;
            }
            stdoutTo.write(ch);
        }
        stdoutFrom.close();
        stdoutTo.close();

        InputStream stderrFrom=process.getErrorStream();
        FileOutputStream stderrTo=new FileOutputStream("stderr.txt");
        while (true){
            int ch=stderrFrom.read();
            if (ch==-1){
                break;
            }
            stderrTo.write(ch);
        }
        stderrFrom.close();
        stderrTo.close();
        int exitCode= 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(exitCode);
    }

}
