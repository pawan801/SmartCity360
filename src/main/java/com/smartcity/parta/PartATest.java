package com.smartcity.parta;

interface DataStream{
    public String writeData(String data);
}

class EnergyMeterDataStream implements DataStream{

    @Override
    public String writeData(String data) {
        return "Raw Energy Data : "+ data;
    }
}

abstract class DataStreamDecorator implements DataStream{

    protected DataStream dataStream;

    public DataStreamDecorator(DataStream dataStream) {
        this.dataStream = dataStream;
    }
}

class EncryptionDecorator extends DataStreamDecorator{

    public EncryptionDecorator(DataStream dataStream) {
        super(dataStream);
    }

    @Override
    public String writeData(String data) {
        return this.dataStream.writeData("DataStream Data Encrypted");
    }
}

class CompressionDecorator extends DataStreamDecorator{

    public CompressionDecorator(DataStream dataStream) {
        super(dataStream);
    }

    @Override
    public String writeData(String data) {
        return this.dataStream.writeData("DataStream Data Compressed") + " With Data Compression";
    }
}

class AuthenticationDecorator extends DataStreamDecorator{

    public AuthenticationDecorator(DataStream dataStream) {
        super(dataStream);
    }

    @Override
    public String writeData(String data) {
        return this.dataStream.writeData("DataStream Data Authenticated") + " With Data Authentication";
    }
}






public class PartATest {
    public static void main(String[] args) {

        String data = "Voltage: 230V, Usage: 45.8kWh";
        DataStream ds1 = new EnergyMeterDataStream();
        System.out.println(ds1.writeData(data));

        DataStream ds2 = new EnergyMeterDataStream();
        ds2 = new EncryptionDecorator(ds2);
        System.out.println(ds2.writeData(data));

        DataStream ds3 = new EnergyMeterDataStream();
        ds3 = new AuthenticationDecorator(ds3);
        System.out.println(ds3.writeData(data));

        DataStream ds4 = new EnergyMeterDataStream();
        ds4 = new CompressionDecorator(ds4);
        System.out.println(ds4.writeData(data));

        DataStream ds5 = new EnergyMeterDataStream();
        ds5 = new EncryptionDecorator(ds5);
        ds5 = new AuthenticationDecorator(ds5);
        ds5 = new CompressionDecorator(ds5);
        System.out.println(ds5.writeData(data));

    }
}
