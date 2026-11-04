package com.smartcity.parta;

import java.util.ArrayList;

interface SmartZoneComponent{
    public void displayStructure(String indend);
    public double getPowerConsumption();
}

class Building implements SmartZoneComponent{

    private String name;
    private double powerConsumption;

    public Building(String name, double powerConsumption) {
        this.name = name;
        this.powerConsumption = powerConsumption;
    }

    @Override
    public void displayStructure(String indent) {
        System.out.println(indent + "Building: " + this.name + " | Power: " + this.powerConsumption + " kW");
    }

    @Override
    public double getPowerConsumption() {
        return this.powerConsumption;
    }
}

class ZoneGroup implements SmartZoneComponent{

    private String name;

    private ArrayList<SmartZoneComponent> components = new ArrayList<>();

    public ZoneGroup(String name) {
        this.name = name;
    }

    public void add(SmartZoneComponent component){
        this.components.add(component);
    }

    public void remove(SmartZoneComponent component){
        this.components.remove(component);
    }

    @Override
    public void displayStructure(String indent) {

        System.out.println(indent + "Zone: " + this.name
                + " [Total Power: " + getPowerConsumption() + " kW]");
        for (SmartZoneComponent c : components) {
            c.displayStructure(indent + "  ");
        }
    }

    @Override
    public double getPowerConsumption() {
        double total = 0.0;

        for(SmartZoneComponent c : components){
            total += c.getPowerConsumption();
        }
        return total;
    }
}






public class PartBTest {

    public static void main(String[] args) {

        SmartZoneComponent b1 = new Building("Commercial Tower A", 850.0);
        SmartZoneComponent b2 = new Building("Techhub Complex", 500);
        SmartZoneComponent hospital = new Building("General Hospital", 850.0);
        SmartZoneComponent b3 = new Building("Sunset Apartment", 850.0);
        SmartZoneComponent b4 = new Building("Green Valley Residencies", 800.0);

        ZoneGroup galleRoad = new ZoneGroup("Galle Road Sector");
        galleRoad.add(b1);
        galleRoad.add(b2);

        ZoneGroup centralDistrict =
                new ZoneGroup("Central District");
        centralDistrict.add(galleRoad);
        centralDistrict.add(hospital);

        ZoneGroup residentialDistrict =
                new ZoneGroup("Residential District");
        residentialDistrict.add(b3);
        residentialDistrict.add(b4);


        System.out.println("Single Building (Hospital): "
                + hospital.getPowerConsumption() + " kW");

        System.out.println("Sub-Zone (Central District): "
                + centralDistrict.getPowerConsumption() + " kW");

        System.out.println("Sub-Zone (Central District): "
                + centralDistrict.getPowerConsumption() + " kW");


    }







}
