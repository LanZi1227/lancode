package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.CodeInValidException;
import common.ProblemNotFoundException;
import compile.Answer;
import compile.Question;
import compile.Task;
import dao.Problem;
import dao.ProblemDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@WebServlet("/compile")
public class CompileServlet extends HttpServlet {
    static class CompileRequest {
        public int id;
        public String code;
    }

     private class CompileResponse {
        public  int error;
        public  String reason;
        public  String stdout;
    }
    private ObjectMapper objectMapper=new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("用户的当前工作目录:"+System.getProperty("use.dir"));
        CompileRequest compileRequest=null;
        CompileResponse compileResponse=new CompileResponse();
        try {
            resp.setStatus(200);
            resp.setContentType("application/json;charset=utf8");
            // 1.先读取请求的正文，并按照JSON 格式进行解析
            String body = readBody(req);
            compileRequest=objectMapper.readValue(body,CompileRequest.class);
            // 2.根据id从数据库中查找到题目的详情=> 得到测试用例代码
            ProblemDAO problemDAO=new ProblemDAO();
            Problem problem=problemDAO.selectOne(compileRequest.id);
            if (problem==null){
                throw new ProblemNotFoundException();
            }
            String testCode=problem.getTestCode();
            String requestCode=compileRequest.code;
            // 3.把用户提交的代码和测试用例代码，给拼接成个完整的代码.
            String finalCode=mergeCode(requestCode,testCode);
            if (finalCode==null){
                throw new CodeInValidException();
            }
            //System.out.println(finalCode);
            // 4.创建一个Task 实例，调用里面的compileAndRun 来进行编译运行
            Task task=new Task();
            Question question=new Question();
            question.setCode(finalCode);
            Answer answer=task.compileAndRun(question);
            // 5.根据Task运行的结果，包装成一个HTTP响应
            compileResponse.error=answer.getError();
            compileResponse.reason=answer.getReason();
            compileResponse.stdout=answer.getStdout();
        } catch (ProblemNotFoundException e) {
            compileResponse.error=3;
            compileResponse.reason="确认没有找到指定题目 id="+compileRequest.id;
        } catch (CodeInValidException e) {
            compileResponse.error=3;
            compileResponse.reason="提交的代码不符合要求";
        }finally {
            String respString=objectMapper.writeValueAsString(compileResponse);
            resp.getWriter().write(respString);
        }


    }
    private static String mergeCode(String requestCode,String testCode){
        int pos=requestCode.lastIndexOf("}");
        if (pos==-1){
            return null;
        }
        String subStr=requestCode.substring(0,pos);
        return subStr+testCode+"\n}";
    }

    private static String readBody(HttpServletRequest req) throws UnsupportedEncodingException {
        // 1.先根据请求头里面的ContentLength 获取到body 的长度(单位是字节)
        int contentLength=req.getContentLength();
        // 2.按照这个长度准备好一个byte[].
        byte[] buffer=new byte[contentLength];
        // 3.通过req 里面的getInputStream 方法，获取到body 的流对象。
        try(InputStream inputStream=req.getInputStream()){
            // 4.基于这个流对象，读取内容，然后把内容放到byte[] 数组中即可.
            inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 5.把这个byte[] 的内容构造成个String
        return new String(buffer,"utf8");
    }
}
