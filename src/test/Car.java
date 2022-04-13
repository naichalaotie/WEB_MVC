package test;

public class Car {
    String Name;
    String color;

    public Car(String name, String color) {
        Name = name;
        this.color = color;
    }

    public Car() {
    }

    @Override
    public String toString() {
        return "Car{" +
                "Name='" + Name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
