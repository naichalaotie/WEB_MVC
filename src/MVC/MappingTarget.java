package MVC;

import java.lang.reflect.Method;

public class MappingTarget {
    String request;
    Object controller;
    Method method;

    @Override
    public String toString() {
        return "MappingTarget{" +
                "request='" + request + '\'' +
                ", controller=" + controller +
                ", method=" + method +
                '}';
    }

    public MappingTarget(String request, Object controller, Method method) {
        this.request = request;
        this.controller = controller;
        this.method = method;
    }

    public String getRequest() {
        return request;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }
}
