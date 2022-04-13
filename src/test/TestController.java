package test;

import MVC.ModelAndView;
import MVC.RequestMapping;
import MVC.RequestParameter;
import MVC.ResponseBody;
public class TestController {

    @RequestMapping("testAutoParseParameter")
    public ModelAndView test1(@RequestParameter("str1")String str1, Car car){
        System.out.println("Car的值为："+car);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setAttribute("str1",str1);
        modelAndView.setAttribute("car",car);
        modelAndView.setViewName("test.jsp");
        return modelAndView;
    }

    @RequestMapping("testForward")
    public String test2(){
        return "test.jsp";
    }

    @RequestMapping("testSend")
    public String test3(){
        return "direct:https://www.baidu.com/";
    }

    @RequestMapping("testReturnJSON")
    @ResponseBody
    public Car test (){
        return new Car("QQ","red");
    }

    public void testConfig(){
        System.out.println("配置文件中的映射关系成功被框架缓存");
    }
}
