package com.example.calculator;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.calculator.databinding.FragmentScientificCalBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScientificCalFragment extends Fragment {

    private FragmentScientificCalBinding binding;
    private String calculation = "";
    private View[] numsButtons;
    private View[] equalButton;
    private View[] delButton;
    private View[] clearButton;
    private View[] opButtons;
    private View[] sciButtons;

    private static Map<String, Integer> operators = new HashMap<>();
    static {
        operators.put("+", 2);
        operators.put("-", 2);
        operators.put("×", 3);
        operators.put("÷", 3);
        operators.put("^", 5);
        operators.put("@", 4);
    }

    private static class Pair<F, S>{
        public F first;
        public S second;
        public Pair(F first, S second){
            this.first = first;
            this.second = second;
        }
    }

    private static Pair<BigDecimal, Integer> calculator(String curCalculation){
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
                        case "^":
                            double powResult = Math.pow(a.doubleValue(), b.doubleValue());
                            numStack.push(new BigDecimal(powResult));
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

    private static boolean parseString(String curCalculation, List<String> participles){
        StringBuilder tmp = new StringBuilder("");
        char[] chars = curCalculation.toCharArray();
        String[] strArray = new String[chars.length];
        for (int i = 0; i < chars.length; i++) {
            strArray[i] = String.valueOf(chars[i]);
        }
        for(int i = 0; i < strArray.length; i++){
            if(operators.containsKey(strArray[i]) || strArray[i].equals("%") || strArray[i].equals("(") || strArray[i].equals(")")){
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

    private static String formatResult(BigDecimal num) {
        BigDecimal absNum = num.abs();
        BigDecimal upperThreshold = new BigDecimal("1000000000000");
        BigDecimal lowerThreshold = new BigDecimal("0.000001");

        if (absNum.compareTo(upperThreshold) >= 0 || (absNum.compareTo(lowerThreshold) <= 0 && absNum.compareTo(BigDecimal.ZERO) != 0)) {
            return String.format("%.6E", num);
        } else {
            return num.stripTrailingZeros().toPlainString();
        }
    }

    private void setMenuOnclick(){
        binding.ivMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_scientific, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_normal) {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    Toast.makeText(requireContext(), "已切换到普通计算器", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_history) {
                    Intent intent = new Intent(requireContext(), logs.class);
                    intent.putExtra(logs.EXTRA_IS_SCIENTIFIC, true);
                    requireActivity().startActivity(intent);
                    Toast.makeText(requireContext(), "已切换到历史记录页面", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void initButtons(){
        numsButtons = new View[]{
                binding.btn0, binding.btn1, binding.btn2, binding.btn3,
                binding.btn4, binding.btn5, binding.btn6, binding.btn7,
                binding.btn8, binding.btn9
        };
        opButtons = new View[]{
                binding.btnMul, binding.btnAdd, binding.btnDiv, binding.btnDot,
                binding.btnSub, binding.btnPercent, binding.btnOpen, binding.btnClose
        };
        sciButtons = new View[]{
                binding.btnSin, binding.btnCos, binding.btnTan, binding.btnLog,
                binding.btnLn, binding.btnExp, binding.btnPow, binding.btnSqrt,
                binding.btnCbrt, binding.btnSq, binding.btn1x, binding.btnPi,
                binding.btnFact, binding.btnYroot
        };
        // 存储功能按钮（仅显示，无功能）
        View[] memoryButtons = new View[]{
                binding.btnMc, binding.btnMPlus, binding.btnMMinus, binding.btnMr,
                binding.btnInv, binding.btnRad
        };
        equalButton = new View[]{binding.btnEqual};
        delButton = new View[]{binding.btnDel};
        clearButton = new View[]{binding.btnClear};
    }

    private void setOnClick(){
        for(View numButton : numsButtons){
            numButton.setOnClickListener(this::onClickNums);
        }
        for(View opButton : opButtons){
            opButton.setOnClickListener(this::onClickOps);
        }
        for(View sciButton : sciButtons){
            sciButton.setOnClickListener(this::onClickSci);
        }
        for(View equButton : equalButton){
            equButton.setOnClickListener(this::onClickEqual);
        }
        for(View deButton : delButton){
            deButton.setOnClickListener(this::onClickDel);
        }
        for(View cButton : clearButton){
            cButton.setOnClickListener(this::onClickC);
        }
    }

    private void onClickNums(View v){
        if(v == binding.btn0){
            calculation += "0";
        } else if(v == binding.btn1){
            calculation += "1";
        } else if (v == binding.btn2) {
            calculation += "2";
        } else if (v == binding.btn3) {
            calculation += "3";
        } else if (v == binding.btn4) {
            calculation += "4";
        } else if(v == binding.btn5){
            calculation += "5";
        } else if(v == binding.btn6){
            calculation += "6";
        } else if(v == binding.btn7){
            calculation += "7";
        } else if(v == binding.btn8){
            calculation += "8";
        } else if(v == binding.btn9){
            calculation += "9";
        }
        updateDisplay();
    }

    private void onClickOps(View v){
        if (v == binding.btnDiv) {
            replaceLastOperator("÷");
        } else if (v == binding.btnAdd) {
            replaceLastOperator("+");
        } else if (v == binding.btnMul) {
            replaceLastOperator("×");
        } else if (v == binding.btnSub) {
            if (calculation.isEmpty() || isLastCharOperator() || calculation.endsWith("(")) {
                calculation += "-";
            } else {
                calculation += "-";
            }
        } else if (v == binding.btnDot) {
            if (!getLastNumber().contains(".")) {
                calculation += ".";
            }
        } else if (v == binding.btnPercent) {
            String lastNum = getLastNumber();
            if (!lastNum.isEmpty() && !lastNum.contains("%")) {
                calculation += "%";
            }
        } else if (v == binding.btnOpen) {
            calculation += "(";
        } else if (v == binding.btnClose) {
            calculation += ")";
        }
        updateDisplay();
    }

    private void onClickSci(View v){
        if (v == binding.btnPi) {
            calculation += "3.141592653589793";
            updateDisplay();
            return;
        }

        if (v == binding.btnPow) {
            calculation += "^";
            updateDisplay();
            return;
        }

        if (v == binding.btnYroot) {
            calculation += "∛";
            updateDisplay();
            return;
        }

        if (v == binding.btnFact) {
            String lastNum = getLastNumber();
            if (!lastNum.isEmpty()) {
                try {
                    int num = (int) Double.parseDouble(lastNum);
                    if (num >= 0 && num <= 20) {
                        long result = 1;
                        for (int i = 2; i <= num; i++) {
                            result *= i;
                        }
                        calculation = calculation.substring(0, calculation.length() - lastNum.length()) + String.valueOf(result);
                        updateDisplay();
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            return;
        }

        String lastNum = getLastNumber();
        if (lastNum.isEmpty() || lastNum.equals("(")) {
            Toast.makeText(requireContext(), "请先输入数字", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double num = Double.parseDouble(lastNum);
            if (Double.isNaN(num) || Double.isInfinite(num)) {
                Toast.makeText(requireContext(), "无效的数字", Toast.LENGTH_SHORT).show();
                return;
            }
            double result = 0;

            if (v == binding.btnSin) {
                result = Math.sin(Math.toRadians(num));
            } else if (v == binding.btnCos) {
                result = Math.cos(Math.toRadians(num));
            } else if (v == binding.btnTan) {
                result = Math.tan(Math.toRadians(num));
            } else if (v == binding.btnLog) {
                if (num > 0) {
                    result = Math.log10(num);
                } else {
                    return;
                }
            } else if (v == binding.btnLn) {
                if (num > 0) {
                    result = Math.log(num);
                } else {
                    return;
                }
            } else if (v == binding.btnExp) {
                result = Math.exp(num);
            } else if (v == binding.btnSqrt) {
                if (num >= 0) {
                    result = Math.sqrt(num);
                } else {
                    return;
                }
            } else if (v == binding.btnCbrt) {
                result = Math.pow(num, 3);
            } else if (v == binding.btnSq) {
                result = Math.pow(num, 2);
            } else if (v == binding.btn1x) {
                if (num != 0) {
                    result = 1.0 / num;
                } else {
                    return;
                }
            }

            calculation = calculation.substring(0, calculation.length() - lastNum.length()) + String.valueOf(result);
        } catch (NumberFormatException e) {
            return;
        }

        updateDisplay();
    }

    private boolean isLastCharOperator() {
        if (calculation.isEmpty()) return false;
        char last = calculation.charAt(calculation.length() - 1);
        return last == '+' || last == '-' || last == '×' || last == '÷' || last == '^';
    }

    private void replaceLastOperator(String newOp) {
        if (calculation.isEmpty()) {
            return;
        }
        if (isLastCharOperator()) {
            calculation = calculation.substring(0, calculation.length() - 1) + newOp;
        } else {
            calculation += newOp;
        }
    }

    private String getLastNumber() {
        if (calculation.isEmpty()) return "";
        int index = -1;
        for (int i = calculation.length() - 1; i >= 0; i--) {
            char c = calculation.charAt(i);
            if (c == '+' || c == '×' || c == '÷' || c == '^' || c == '(') {
                index = i;
                break;
            }
            if (c == '-' && i > 0) {
                char prev = calculation.charAt(i - 1);
                if (prev == '+' || prev == '-' || prev == '×' || prev == '÷' || prev == '^' || prev == '(' || prev == 'E' || prev == 'e') {
                    index = i;
                    break;
                }
            }
        }
        return index == -1 ? calculation : calculation.substring(index + 1);
    }

    private void updateDisplay() {
        binding.tvExpression.setText(calculation);
        Pair<BigDecimal, Integer> pair = calculator(calculation);
        if(pair.second == 0){
            binding.tvResult.setText(formatResult(pair.first));
        } else {
            binding.tvResult.setText("");
        }
    }

    private void onClickEqual(View v){
        Pair<BigDecimal, Integer> pair = calculator(calculation);
        if(pair.second == 0){
            String result = formatResult(pair.first);
            // 保存历史记录
            LogDatabaseHelper.saveRecord(calculation, result);
            
            binding.tvExpression.setText(result);
            // 清空算式，下一次按键从空串开始
            calculation = "";
            binding.tvResult.setText("");
        } else if(pair.second == 1){
            binding.tvResult.setText("除数不能为零");
        } else {
            binding.tvResult.setText("错误");
        }
    }

    private void onClickDel(View v){
        if(calculation.isEmpty()) return;
        calculation = calculation.substring(0, calculation.length() - 1);
        updateDisplay();
    }

    private void onClickC(View v){
        calculation = "";
        binding.tvExpression.setText("");
        binding.tvResult.setText("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentScientificCalBinding.inflate(inflater, container, false);
        initButtons();
        setOnClick();
        setMenuOnclick();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}