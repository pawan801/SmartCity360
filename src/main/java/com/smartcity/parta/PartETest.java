package com.smartcity.parta;

interface Expression{
    public int interpret();
}

class TerminalExpression implements Expression{
    private int value;

    public TerminalExpression(int value) {
        this.value = value;
    }

    @Override
    public int interpret() {
        return value;
    }
}

class NonTerminalExpression1 implements Expression{

    private Expression e1;
    private Expression e2;

    public NonTerminalExpression1(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public int interpret() {
        return e1.interpret() + e2.interpret();
    }
}

class NonTerminalExpression2 implements Expression{

    private Expression e1;
    private Expression e2;

    public NonTerminalExpression2(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public int interpret() {
        return e1.interpret() - e2.interpret();
    }
}

public class PartETest {

    public static void main(String[] args) {
        Expression sectorLoad = new NonTerminalExpression1(
                new TerminalExpression(120),
                new TerminalExpression(180)
        );
        System.out.println("1. Sector Power Consumption (Zone A + Zone B): "
                + sectorLoad.interpret() + " kW");

        Expression totalCityLoad = new NonTerminalExpression1(
                new NonTerminalExpression1(
                        new TerminalExpression(120),
                        new TerminalExpression(180)
                ),
                new TerminalExpression(350)
        );
        System.out.println("2. Total Metropolitan Grid Load (Nested AST): "
                + totalCityLoad.interpret() + " kW");

        Expression remainingQuota = new NonTerminalExpression2(
                new TerminalExpression(800),
                totalCityLoad
        );
        System.out.println("3. Municipal Quota Margin (Allowed 800 kW - Total Load): "
                + remainingQuota.interpret() + " kW");
    }
}
