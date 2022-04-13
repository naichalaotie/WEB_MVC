package MVC;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String viewName;
    private Map<String,Object> datas = new HashMap<>();

    public void setViewName(String view){
        viewName = view;
    }

    public String getViewName(){
        return viewName;
    }

    public void setDatas(HashMap map){
        datas = map;
    }

    public Map<String,Object> getDatas(){
        return datas;
    }

    public void setAttribute(String key,Object val){
        datas.put(key,val);
    }
}
