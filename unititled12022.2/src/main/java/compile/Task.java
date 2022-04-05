package compile;
import common.FileUtil;

import java.io.File;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class Task {
    //通过一组常量来约定临时文件的名字
    //这个表示所有临时文件所在的目录
    private  String WORK_DIR="./tmp/";
    //约定代码的类名
    private   String CLASS=null;
    //要编译的代码文件
    private   String CODE=null;
    //存放编译错误信息的文件
    private   String COMPILE_ERROR=null;
    //存放运行时的标准输出
    private   String STDOUT=null;
    //存放运行时标准错误
    private   String STDERR=null;


    public Task(){
        WORK_DIR="./tmp/"+ UUID.randomUUID().toString()+"/";
        CLASS="Solution";
        CODE=WORK_DIR+"Solution.java";
        COMPILE_ERROR=WORK_DIR+"compileError.txt";
        STDOUT=WORK_DIR+"stdout.txt";
        STDERR=WORK_DIR+"stderr.txt";
    }


    public Answer compileAndRun(Question question){
        Answer answer=new Answer();
        File workDir=new File(WORK_DIR);
        if (!workDir.exists()){
            workDir.mkdirs();
        }

        if (!checkCodeSafe(question.getCode())) {
            System.out.println("用户提交了不安全的代码!");
            answer.setError(3);
            answer.setReason("您提交的代码可能会危害到服务器, 禁止运行!");
            return answer;
        }
        //1.把question中的code写入到一个Solution.java文件中
        FileUtil.writeFile(CODE,question.getCode());
        //2.创建子进程，调用javac进行编译，编译时候需要有一个.java文件
        //    如果编译出错，javac就会把错误信息给写入到stderr里，就可以用一个专门的文件来保存compileError.txt
        //需要先把编译命令构造出来
        String compileCmd=String.format("javac -encoding utf8 %s -d %s",CODE,WORK_DIR);
        System.out.println("编译命令"+compileCmd);
        CommandUtil.run(compileCmd,null,COMPILE_ERROR);
        //如果编译出错了，错误信息就被记录到COMPILE_ERROR这个文件中，没有出错，空文件
        String compileError= FileUtil.readFile(COMPILE_ERROR);
        if (!compileError.equals("")){
            //编译出错，返回Answer
            System.out.println("编译出错");
            answer.setError(1);
            answer.setReason(compileError);
            return answer;
        }
        //编译正确
        String runCmd=String.format("java -classpath %s %s",WORK_DIR,CLASS);
        System.out.println("运行命令"+runCmd);
        CommandUtil.run(runCmd,STDOUT,STDERR);
        String runError= FileUtil.readFile(STDERR);
        if (!runError.equals("")){
            System.out.println("运行出错");
            answer.setError(2);
            answer.setReason(runError);
            return answer;
        }
        answer.setError(0);
        answer.setStdout(FileUtil.readFile(STDOUT));
        return answer;

    }

    private boolean checkCodeSafe(String code) {
        List<String> blackList = new ArrayList<>();
        // 防止提交的代码运行恶意程序
        blackList.add("Runtime");
        blackList.add("exec");
        // 禁止提交的代码读写文件
        blackList.add("java.io");
        // 禁止提交的代码访问网络
        blackList.add("java.net");

        for (String target : blackList) {
            int pos = code.indexOf(target);
            if (pos >= 0) {
                // 找到任意的恶意代码特征, 返回 false 表示不安全
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Task task=new Task();
        Question question=new Question();
        question.setCode("public class Solution {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"hello world\");\n" +
                "    }\n" +
                "}");
        Answer answer=task.compileAndRun(question);
        System.out.println(answer);

    }
}
