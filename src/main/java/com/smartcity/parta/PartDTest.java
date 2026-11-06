package com.smartcity.parta;

import java.util.ArrayList;

abstract class AbstractStreetlight{
    public abstract void display();
}

class SharedStreetlight{
    private String model;
    private int wattage;
    private String colorTemp;

    public SharedStreetlight(String model, int wattage, String colorTemp) {
        this.model = model;
        this.wattage = wattage;
        this.colorTemp = colorTemp;
    }

    public String getModel() {
        return model;
    }

    public int getWattage() {
        return wattage;
    }

    public String getColorTemp() {
        return colorTemp;
    }
}

class SharedStreetlightPool{

private static final ArrayList<SharedStreetlight> pool = new ArrayList<>();

public static SharedStreetlight getSharedStreetlight(String model, int wattage, String colorTemp){

      SharedStreetlight light = null;

      for(SharedStreetlight item : pool){
          if (model.equals(item.getModel()) && wattage == item.getWattage() && colorTemp.equals(item.getColorTemp())) {
              light = item;
              break;
          }
      }
      if (light == null) {
          light = new SharedStreetlight(model, wattage, colorTemp);
          pool.add(light);
      }
      return light;
  }
}

class Streetlight extends AbstractStreetlight{
    private String id;
    private double latitude;
    private double longitude;
    private int brightness;
    private SharedStreetlight sharedStreetlight;

    public Streetlight(String model, int wattage, String colorTemp,
                       String id, double latitude, double longitude, int brightness) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.brightness = brightness;
        this.sharedStreetlight = sharedStreetlight;
        this.sharedStreetlight = SharedStreetlightPool.getSharedStreetlight(model, wattage, colorTemp);
    }

    @Override
    public void display() {
        System.out.println("Light ID: " + this.id);
        System.out.println("Model: " + this.sharedStreetlight.getModel());
        System.out.println("Wattage: " + this.sharedStreetlight.getWattage() + "W");
        System.out.println("Color Temp: " + this.sharedStreetlight.getColorTemp());
        System.out.println("GPS: (" + this.latitude + ", " + this.longitude + ")");
        System.out.println("Brightness: " + this.brightness + "%");
        System.out.println("Shared Instance Ref: " + this.sharedStreetlight);
        System.out.println("---------------------------------------------");
    }
}


public class PartDTest {

    public static void main(String[] args) {

        Streetlight sl1 = new Streetlight("LED-LUMEN-60W", 60, "4000K",
                "SL-COL-001", 6.9271, 79.8612, 80);

        Streetlight sl2 = new Streetlight("HIGH-MAST-150W", 150, "5000K",
                "SL-COL-002", 6.9275, 79.8615, 100);

        Streetlight sl3 = new Streetlight("LED-LUMEN-60W", 60, "4000K",
                "SL-COL-003", 6.9280, 79.8620, 60);

        Streetlight sl4 = new Streetlight("LED-LUMEN-60W", 60, "4000K",
                "SL-COL-004", 6.9285, 79.8625, 90);

        System.out.println("=== Smart City IoT Streetlight Telemetry ===");
        sl1.display();
        sl2.display();
        sl3.display();
        sl4.display();


    }
}



