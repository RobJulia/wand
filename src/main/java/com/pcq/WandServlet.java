package com.pcq;

import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pcq on 2017/5/17.
 */

public class WandServlet extends HttpServlet {
    Map<String, Object> beanMap = new HashMap<String, Object>();
    Map<String, WandMethod> wandMethodMap = new HashMap<String, WandMethod>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //返回方法列表
        String requestURI = req.getRequestURI();
        PrintWriter printWriter = resp.getWriter();
        printWriter.println("<html>");
        printWriter.println("<head>");
        printWriter.println("<h1>wand </h1><body>");
        if (requestURI.startsWith("/wand/methodList")) {
            printWriter.println("服务:方法<p>");
            for (String methodName : wandMethodMap.keySet()) {
                String className = wandMethodMap.get(methodName).getClassName();
                printWriter.println(className + ":" + methodName + "<a href=\"/wand/methodInfo?className=" + className + "&methodName=" + methodName + "\">调用</a><p>");
            }
        } else if (requestURI.startsWith("/wand/methodInfo")) {
            String className = req.getParameter("className");
            String methodName = req.getParameter("methodName");
            WandMethod wandMethod = wandMethodMap.get(methodName);
            printWriter.println("服务名：" + className + "<p>");
            printWriter.println("方法名：" + methodName + "<p>");
            printWriter.println("<form action=\"/wand/invoke\" method=\"POST\"><table border=1px>");
            printWriter.println("<tr><th>类型</th><th>参数名</th><th>参数值</th></tr>");
            printWriter.println("<input name=\"className\" hidden value=\"" + className + "\"></input>");
            printWriter.println("<input name=\"methodName\" hidden value=\"" + methodName + "\"></input>");

            int i = 1;
            for (WandParameter parameter : wandMethod.getParameters()) {
                printWriter.println("<tr><td>" + parameter.getType() + "</td><td>" + parameter.getName() + "</td><td><input name=\"field" + i + "\"></input></td></tr>");
                i++;
            }
            printWriter.println("</table>");
            printWriter.println("<input type=submit></input></form>");
        }
        printWriter.println("</body></head>");
        printWriter.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        if (requestURI.startsWith("/wand/invoke")) {
            String className = req.getParameter("className");
            String methodName = req.getParameter("methodName");
            Map<String, String[]> parameterMap = req.getParameterMap();
            Object o = beanMap.get(className);
            com.pcq.WandMethod wandMethod = wandMethodMap.get(methodName);
            int count = 0;
            List<Object> paramList = new ArrayList<Object>();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                if (key.equals("className") || key.equals("methodName")) {
                    continue;
                }
                paramList.add(entry.getValue()[0]);
                count++;
            }
            Object[] objects = new Object[count];
            for (int i = 0; i < count; i++) {
                objects[i] = paramList.get(i);

            }
            Method method = wandMethod.getMethod();
            try {
                method.invoke(o, objects);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            System.out.println(req.getParameterNames());

        }
    }

    @Override
    public void init() {
        ApplicationContext applicationContext = WandApplicationContextUtil.getApplicationContext();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object o = applicationContext.getBean(beanName);
            Class clazz = o.getClass();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                com.pcq.annotation.WandMethod annotation = method.getAnnotation(com.pcq.annotation.WandMethod.class);
                if (annotation != null) {
                    beanMap.put(clazz.getName(), o);
                    wandMethodMap.put(method.getName(), builldWandMethod(method, clazz.getName(), annotation.params()));
                }
            }
        }
    }

    private WandMethod builldWandMethod(Method method, String className, String params) {
        WandMethod wandMethod = new WandMethod();
        List<WandParameter> paramterList = new ArrayList<WandParameter>();
        wandMethod.setClassName(className);
        wandMethod.setMethodName(method.getName());
        wandMethod.setMethod(method);
        Class[] parameterTypes = method.getParameterTypes();
        String[] paramNames = null;
        if (!StringUtils.isEmpty(params)) {
            paramNames = params.split("&");
        }
        int i = 0;
        for (Class clazz : parameterTypes) {
            WandParameter wandParameter = new WandParameter();
            wandParameter.setType(clazz.getName());
            if (paramNames != null) {
                wandParameter.setName(paramNames[i]);
                i++;
            }

            paramterList.add(wandParameter);
        }
        wandMethod.setParameters(paramterList);
        return wandMethod;
    }

    public static void main(String[] args) {
        Class clazz = new UserService().getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            Class[] classes = method.getParameterTypes();


        }

    }

}
