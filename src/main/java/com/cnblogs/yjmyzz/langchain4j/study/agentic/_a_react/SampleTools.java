package com.cnblogs.yjmyzz.langchain4j.study.agentic._a_react;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author junmingyang
 */
@Component("sampleTools")
public class SampleTools {

    @Tool("计算两个数的加法运算")
    public String add(double a, double b) {
        double result = a + b;
        System.out.printf("[工具调用] 加法: %.2f + %.2f = %.2f\n", a, b, result);
        return String.format("%.2f + %.2f = %.2f", a, b, result);
    }

    @Tool("计算两个数的减法运算")
    public String subtract(double a, double b) {
        double result = a - b;
        System.out.printf("[工具调用] 减法: %.2f - %.2f = %.2f\n", a, b, result);
        return String.format("%.2f - %.2f = %.2f", a, b, result);
    }

    @Tool("计算两个数的乘法运算")
    public String multiply(double a, double b) {
        double result = a * b;
        System.out.printf("[工具调用] 乘法: %.2f × %.2f = %.2f\n", a, b, result);
        return String.format("%.2f × %.2f = %.2f", a, b, result);
    }

    @Tool("计算两个数的除法运算")
    public String divide(double a, double b) {
        if (b == 0) {
            return "错误：除数不能为零";
        }
        double result = a / b;
        System.out.printf("[工具调用] 除法: %.2f ÷ %.2f = %.2f\n", a, b, result);
        return String.format("%.2f ÷ %.2f = %.2f", a, b, result);
    }

    @Tool("获取当前日期和时间")
    public String getCurrentDateTime() {
        String datetime = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        System.out.println("[工具调用] 当前时间: " + datetime);
        return datetime;
    }

    @Tool("查询指定城市的天气信息")
    public String getWeather(String city) {
        System.out.println("[工具调用] 查询天气: " + city);
        // 这里可以集成真实的天气API
        return String.format("%s的天气：晴转多云，温度22-28°C，湿度65%%，东南风2级", city);
    }

    @Tool("计算圆的面积")
    public String calculateCircleArea(double radius) {
        double area = Math.PI * radius * radius;
        System.out.printf("[工具调用] 圆面积计算: 半径=%.2f, 面积=%.2f\n", radius, area);
        return String.format("半径为 %.2f 的圆的面积是 %.2f", radius, area);
    }

    @Tool("计算长方体体积")
    public String calculateCuboidVolume(double length, double width, double height) {
        double volume = length * width * height;
        System.out.printf("[工具调用] 长方体体积: %.2f×%.2f×%.2f=%.2f\n",
                length, width, height, volume);
        return String.format("长 %.2f、宽 %.2f、高 %.2f 的长方体体积是 %.2f",
                length, width, height, volume);
    }
}
