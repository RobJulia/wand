package com.pcq;

import com.alibaba.fastjson.JSONObject;
import com.pcq.annotation.WandMethod;
import com.pcq.utils.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
 * Created by Administrator on 2017/5/25 0025.
 */
public class WandStandAloneServlet extends HttpServlet implements ApplicationContextAware {
    Map<String, Object> beanMap = new HashMap<String, Object>();
    Map<String, WMethod> wandMethodMap = new HashMap<String, WMethod>();
    private static ApplicationContext applicationContext = null;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //返回方法列表
        String requestURI = req.getRequestURI();
        PrintWriter printWriter = resp.getWriter();
        printWriter.println("<html>");
        printWriter.println("<head> ");
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
            WMethod wMethod = wandMethodMap.get(methodName);
            printWriter.println("服务名：" + className + "<p>");
            printWriter.println("方法名：" + methodName + "<p>");
            printWriter.println("<form action=\"/wand/invoke\" method=\"POST\"><table>");
            printWriter.println("<tr><th>类型</th><th>参数名</th><th>参数值</th></tr>");
            printWriter.println("<input name=\"className\" hidden value=\"" + className + "\"></input>");
            printWriter.println("<input name=\"methodName\" hidden value=\"" + methodName + "\"></input>");

            int i = 1;
            for (WParameter parameter : wMethod.getParameters()) {
                String name = parameter.getName() == null ? "" : parameter.getName();
                printWriter.println("<tr><td>" + parameter.getType() + "</td><td>" + name + "</td><td><input name=\"field" + i + "\"></input></td></tr>");

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
            Object bean = beanMap.get(className);
            WMethod wMethod = wandMethodMap.get(methodName);
            int count = 0;
            List<String> paramList = new ArrayList<String>();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                if (key.equals("className") || key.equals("methodName")) {
                    continue;
                }
                paramList.add(entry.getValue()[0]);
                count++;
            }
            Object[] objects = new Object[count];
            List<WParameter> wParameterList = wMethod.getParameters();
            PrintWriter printWriter = resp.getWriter();

            for (int i = 0; i < count; i++) {
                WParameter wParameter = wParameterList.get(i);
                BuildParameterRes buildParameterRes = buildParameter(paramList.get(i), wParameter.getType());
                if (!buildParameterRes.isSuccess()) {
                    printWriter.println("第"+(i+1)+"参数非法");
                    return;
                }
                objects[i] = buildParameterRes.getData();

            }
            Method method = wMethod.getMethod();
            try {
                Object res = method.invoke(bean, objects);
                printWriter.println(JSONObject.toJSONString(res));

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }
    class BuildParameterRes{
        private boolean success;
        private String errMsg;
        private Object data;
        public BuildParameterRes(boolean scuccess,Object o){
            this.success=scuccess;
            this.data=o;
        }
        public BuildParameterRes(boolean scuccess,String errMsg){
            this.success=scuccess;
            this.errMsg=errMsg;
        }
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
        public  BuildParameterRes Success(Object o){
            return new BuildParameterRes(true,o);
        }
    }
    private BuildParameterRes buildParameter(String o, String type) {
        try {
            //基本类型
            if (type.equals("java.lang.String")) {
                return new BuildParameterRes(true,o);
            }

            if (type.equals("int")) {
                if (StringUtils.isEmpty(o)) {
                    return new BuildParameterRes(true,0);
                }
                int a = Integer.valueOf(o);
                return new BuildParameterRes(true,a);
            }
            if (type.equals("long")) {
                if (StringUtils.isEmpty(o)) {
                    return new BuildParameterRes(true,0l);
                }
                long a = Long.valueOf(o);
                return new BuildParameterRes(true,0l);
            }
            if (type.equals("float")) {
                if (StringUtils.isEmpty(o)) {
                    return new BuildParameterRes(true,0f);
                }
                float a = Float.valueOf(o);
                return new BuildParameterRes(true,a);
            }
            if (type.equals("double")) {
                if (StringUtils.isEmpty(o)) {
                    return new BuildParameterRes(true,0f);
                }
                double a = Double.valueOf(o);
                return new BuildParameterRes(true,a);
            }
            if (type.equals("boolean")) {
                if (StringUtils.isEmpty(o)) {
                    return new BuildParameterRes(true,false);
                }
                if (o.equalsIgnoreCase("true")) {
                    return new BuildParameterRes(true,true);
                }
                return new BuildParameterRes(true,false) ;
            }
            //包装类型
            if (StringUtils.isEmpty(o)) {
                return new BuildParameterRes(true,null);
            }
            if (type.equals("java.lang.Integer")) {
                return new BuildParameterRes(true,Integer.valueOf(o));
            }
            if (type.equals("java.lang.Long")) {
                return new BuildParameterRes(true,Long.valueOf(o));
            }

            if (type.equals("java.lang.Boolean")) {
                if (o.equalsIgnoreCase("true")) {
                    return new BuildParameterRes(true,Boolean.TRUE);
                }
                return new BuildParameterRes(true,Boolean.FALSE);
            }
            if (type.equals("java.lang.Float")) {
                return new BuildParameterRes(true,Float.valueOf(o));
            }
            if (type.equals("java.lang.Double")) {
                return new BuildParameterRes(true,Double.valueOf(o));
            }
        } catch (NumberFormatException e) {
            return new BuildParameterRes(false,"参数类型错误");
        }
        return new BuildParameterRes(false,"参数类型错误");

    }

    @Override
    public void init() {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class clazz = bean.getClass();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                com.pcq.annotation.WandMethod annotation = method.getAnnotation(com.pcq.annotation.WandMethod.class);
                if (annotation != null) {
                    String clazzName = clazz.getName();
                    if (null == beanMap.get(clazzName)) {
                        beanMap.put(clazzName, bean);
                    }
                    wandMethodMap.put(method.getName(), builldWandMethod(method, clazzName, annotation));
                }
            }
        }
    }

    private WMethod builldWandMethod(Method method, String className, WandMethod anotation) {
        WMethod wMethod = new WMethod();
        wMethod.setClassName(className);
        wMethod.setMethodName(method.getName());
        wMethod.setMethod(method);
        wMethod.setMethodDesc(anotation.desc());
        String params = anotation.params();

        List<WParameter> parameterList = new ArrayList<WParameter>();
        Class[] parameterTypes = method.getParameterTypes();
        String[] paramNames = params.split("&");
        int parameterSeq = 0;
        for (Class parameterClass : parameterTypes) {
            WParameter wParameter = new WParameter();
            wParameter.setType(parameterClass.getName());
            if (paramNames.length > parameterSeq) {
                wParameter.setName(paramNames[parameterSeq]);
            }
            parameterSeq++;
            parameterList.add(wParameter);
        }

        wMethod.setParameters(parameterList);
        return wMethod;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
