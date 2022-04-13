package MVC;

import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    private Map<String,MappingTarget> mappingTargets = new HashMap<>();
    private Map<Class,Object> controllers = new HashMap<>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
                                                                    System.out.println("DispatcherServlet接收到了请求");
            String request = req.getRequestURI();
            String reqstr = request.substring(request.lastIndexOf("/")+1) ;
            MappingTarget mappingTarget = mappingTargets.get(reqstr);
            if(mappingTarget==null){
                resp.sendError(404,"["+ request +"]");//req.getRequestURI()
                return;
            }
            Method method = mappingTarget.getMethod();
            Object controller = mappingTarget.getController();
            Object result;
            Parameter[] parameters = method.getParameters();
            if(parameters==null||parameters.length==0){
                result = method.invoke(controller);
            }else{
                List<Object> parameterValues = new ArrayList<>();
                for (Parameter parameter : parameters){
                     RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
                     if(requestParameter!=null){
                         String key = requestParameter.value();
                         String value = req.getParameter(key);
                         if(value==null){
                             parameterValues.add(null);
                         }
                         Class clazz = parameter.getType();
                         Object obj = caseType(value,clazz);

                         parameterValues.add(obj);
                     }else{
                         //没有requestParameter注解
                        Class parameterType = parameter.getType();
                        //Class clazz = parameter.getClass();
                        if(parameterType == HttpServletRequest.class){
                            parameterValues.add(req);
                        }else if(parameterType == HttpServletResponse.class){
                            parameterValues.add(resp);
                        }else if(parameterType==HttpServlet.class){
                            parameterValues.add(req.getSession());
                        }else{
                            //Class clazz = parameter.getClass();
                            //对象参数
                            Object obj = parameterType.newInstance();
                            Method[] methods = parameterType.getMethods();
                            for(Method m : methods){
                                String methodName = m.getName();
                                if(methodName.startsWith("set")){
                                    String fieldName = methodName.substring(3);
                                    if(fieldName.length()==1){
                                        fieldName.toLowerCase();
                                    }else{
                                        fieldName = fieldName.substring(0,1).toLowerCase()+fieldName.substring(1);
                                    }
                                    String parameterValue = req.getParameter(fieldName);
                                    Class paramType = m.getParameterTypes()[0];
                                    Object value = caseType(parameterValue,paramType);
                                    m.invoke(obj,value);
                                }
                            }
                            parameterValues.add(obj);
                        }
                     }
                }
                //parameterValue是集合
                result = method.invoke(controller,parameterValues.toArray());
            }

            ResponseBody responseBody= method.getAnnotation(ResponseBody.class);
            if(result!=null) {
                if (responseBody == null) {
                    if (result instanceof String) {
                        String $re = (String) result;
                        if ($re.startsWith("direct:")) {
                            $re = $re.substring($re.indexOf(":") + 1);
                            resp.sendRedirect($re);
                        } else {
                            req.getRequestDispatcher($re).forward(req, resp);
                        }
                    } else if (result.getClass() == ModelAndView.class) {
                        System.out.println("返回的是ModelAndView");
                        ModelAndView modelAndView = (ModelAndView) result;
                        String viewName = modelAndView.getViewName();
                        Map<String, Object> map = modelAndView.getDatas();
                        System.out.println("modelAndView里面的数据：" + map);
                        Set<String> keys = map.keySet();
                        for (String key : keys) {
                            req.setAttribute(key, map.get(key));
                        }
                        req.getRequestDispatcher(viewName).forward(req, resp);

                    }
                } else {
                    //注解不为空（response Body）
                    //直接响应
                    if (result instanceof String || result instanceof Double || result instanceof Float || result instanceof Integer) {
                        resp.getWriter().write(result.toString());
                    } else {
                        String str = JSON.toJSONString(result);
                        resp.getWriter().write(str);
                    }
                }
            }
       //     method.invoke(controller,parameterValues);
            resp.setContentType("text/html;charset = UTF-8");
            //resp.setCharacterEncoding("UTF-8");
            //resp.getWriter().write("测试一下");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void init() throws ServletException {
        //扫描配置文件
        try{
                                                                                            System.out.println("开始扫描配置文件");
            String configLocation = super.getInitParameter("configLocation");
            if(configLocation!=null&&!"".equals(configLocation)){
                InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(configLocation);
                Properties p = new Properties();
                p.load(in);
                Enumeration keys = p.propertyNames();
                while(keys.hasMoreElements()){
                    String request = (String)keys.nextElement();
                    String conAndMeth = p.getProperty(request);
                    int index = conAndMeth.lastIndexOf(".");
                    String conStr = conAndMeth.substring(0,index);
                    String methStr = conAndMeth.substring(index+1);
                    Class clazz = Class.forName(conStr);
                    Object controller  = getSingleController(clazz);
                    Method[] methods  = clazz.getMethods();
                    for(Method method : methods){
                        if(method.getName().equals(methStr)){
                            mappingTargets.put(request,new MappingTarget(request,controller,method));
                            break;
                        }
                    }

                }
            }
                                                                                         System.out.println("配置文件扫描完毕");
        }catch(Exception e){
            e.printStackTrace();
        }

        //=======================================扫描注解================================================
        try{
            String packagePath =  super.getInitParameter("Scanner");
                                                                                                System.out.println("注解扫描开始进行");
            if(packagePath!=null && !" ".equals(packagePath)){
                String[] paths = packagePath.split(",");
                for (String pacStr : paths){
                    pacStr = pacStr.trim();
                    String dirPath = pacStr.replace(".","/");
                    //Thread.currentThread().getContextClassLoader()
                    //                            .getResource(dirPath).getPath();//c:/xxxx/src/com/duyi/test3
                    dirPath = Thread.currentThread().getContextClassLoader().getResource(dirPath).getPath();
                    File dir = new File(dirPath);
                    File[] files =  dir.listFiles();
                    if(files!=null){
                        for(File file : files){
                            String fileName = file.getName();
                            int index = fileName.indexOf(".");
                            String className = fileName.substring(0,index);
                            String classPath = pacStr+"."+className;
                            Class clazz = Class.forName(classPath);
                            Object controller = getSingleController(clazz);
                            if (controller==null){
                                System.out.println("配置或类可能有些小问题");
                                continue;
                            }
                            Method[] methods = clazz.getMethods();
                            if(methods!=null){
                                for (Method method : methods){
                                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                                    if(requestMapping==null){
                                        continue;
                                    }
                                    String request = requestMapping.value();
                                    MappingTarget mappingTarget = new MappingTarget(request,controller,method);
                                    mappingTargets.put(request,mappingTarget);
                                }
                            }
                        }
                    }
                }
            }
                                                                                System.out.println("注解扫描运行完毕");
        }catch (Exception exception){

        }
        System.out.println(mappingTargets);
    }




    private Object getSingleController(Class clazz) throws Exception {
            Object obj = controllers.get(clazz);
            if(obj==null) {
                try{
                    //obj = clazz.newInstance();
                    obj = clazz.newInstance();
                }catch(Exception E){
//                    System.out.println("有错误");
                }
                //obj = clazz.newInstance();
            }
        return obj;
    }

    private Object caseType(String value,Class type){
        if(type == String.class){
            return value ;
        }else if(type == int.class || type == Integer.class){
            return Integer.parseInt(value) ;
        }else if(type == double.class || type == Double.class){
            return Double.parseDouble(value);
        }
        return value ;
    }


}
