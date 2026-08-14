package com.example.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Calculator {
    private static Map<String, Integer> operators = new HashMap<>();
    {
        operators.put("+", 2);
        operators.put("-", 2);
        operators.put("×", 3);
        operators.put("÷", 3);
        operators.put("@", 4);
    }
    public static class Pair<F, S>{
        public F first;
        public S second;
        public Pair(F first, S second){
            this.first = first;
            this.second = second;
        }
    }
    public static Pair<BigDecimal, Integer> calculator(String curCalculation){
        if(curCalculation.isEmpty()){
            return new Pair<>(null, 2);
        }
        List<String> participles = new ArrayList<>();
        if(!parseString(curCalculation, participles)){
            return new Pair<>(null, 2);
        }
        List<String> result = new ArrayList<>();
        Deque<String> opStack = new ArrayDeque<>();
        for(String s : participles){
            if(s.equals("%")){
                result.add(s);
            } else if(operators.containsKey(s)){
                while(!opStack.isEmpty() && !opStack.peek().equals("(") && operators.get(opStack.peek()) >= operators.get(s)){
                    result.add(opStack.remove());
                }
                opStack.push(s);
            } else if(s.equals("(")){
                opStack.push(s);
            } else if (s.equals(")")) {
                while(!opStack.isEmpty() && !opStack.peek().equals("(")){
                    result.add(opStack.remove());
                }
                if(opStack.isEmpty()){
                    return new Pair<>(null, 2);
                }
                opStack.pop();
            } else {
                result.add(s);
            }
        }
        while(!opStack.isEmpty()){
            String op = opStack.remove();
            if(op.equals("(")){
                return new Pair<>(null, 2);
            }
            result.add(op);
        }
        Deque<BigDecimal> numStack = new ArrayDeque<>();
        for(String s : result){
            if(operators.containsKey(s)){
                if(s.equals("@")){
                    if(numStack.isEmpty()){
                        return new Pair<>(null, 2);
                    }
                    BigDecimal num = numStack.pop();
                    numStack.push(num.negate());
                } else {
                    if(numStack.size() < 2){
                        return new Pair<>(null, 2);
                    }
                    BigDecimal b = numStack.pop();
                    BigDecimal a = numStack.pop();
                    switch (s) {
                        case "+":
                            numStack.push(a.add(b));
                            break;
                        case "-":
                            numStack.push(a.subtract(b));
                            break;
                        case "×":
                            numStack.push(a.multiply(b));
                            break;
                        case "÷":
                            if(b.compareTo(BigDecimal.ZERO) == 0){
                                return new Pair<>(null, 1);
                            }
                            numStack.push(a.divide(b, 10, RoundingMode.HALF_UP));
                            break;
                    }
                }
            } else if(s.equals("%")){
                if(numStack.isEmpty()){
                    return new Pair<>(null, 2);
                }
                BigDecimal num = numStack.pop();
                numStack.push(num.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
            } else {
                try {
                    numStack.push(new BigDecimal(s));
                } catch (Exception e){
                    return new Pair<>(null, 2);
                }
            }
        }
        if(numStack.size() != 1){
            return new Pair<>(null, 2);
        }
        return new Pair<>(numStack.pop(), 0);
    }
    private boolean maybeValid(String calculation){
        return true;
    }
    private static boolean parseString(String curCalculation, List<String> participles){
        StringBuilder tmp = new StringBuilder("");
        char[] chars = curCalculation.toCharArray();
        String[] strArray = new String[chars.length];
        for (int i = 0; i < chars.length; i++) {
            strArray[i] = String.valueOf(chars[i]);
        }
        for(int i = 0; i < strArray.length; i++){
            if(operators.containsKey(strArray[i]) || strArray[i].equals("%")){
                if(tmp.length() != 0){
                    String num = tmp.toString();
                    if(num.length() == 1 && num.charAt(0) == '.'){
                        return false;
                    }
                    if(num.charAt(0) == '.'){
                        num = "0" + num;
                    } else if(num.charAt(num.length() - 1) == '.'){
                        num = num.replace(".", "");
                    }
                    participles.add(num);
                    tmp.setLength(0);
                }
                if(strArray[i].equals("-") && (i == 0 || (i > 0 && operators.containsKey(strArray[i-1]) || strArray[i-1].equals("(")))){
                    participles.add("@");
                } else {
                    participles.add(strArray[i]);
                }
            } else {
                tmp.append(strArray[i]);
            }
        }
        String num = tmp.toString();
        if(num.isEmpty()){
            return true;
        }
        if(num.length() == 1 && num.charAt(0) == '.'){
            return false;
        }
        if(num.charAt(0) == '.'){
            num = "0" + num;
        } else if(num.charAt(num.length() - 1) == '.'){
            num = num.replace(".", "");
        }
        participles.add(num);
        tmp.setLength(0);
        return true;
    }

    public static String formatResult(BigDecimal num) {
        BigDecimal absNum = num.abs();
        BigDecimal upperThreshold = new BigDecimal("1000000000000");
        BigDecimal lowerThreshold = new BigDecimal("0.000001");

        if (absNum.compareTo(upperThreshold) >= 0 || (absNum.compareTo(lowerThreshold) <= 0 && absNum.compareTo(BigDecimal.ZERO) != 0)) {
            return String.format("%.6E", num);
        } else {
            return num.stripTrailingZeros().toPlainString();
        }
    }
}
