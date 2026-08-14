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

import com.example.calculator.databinding.FragmentNormalCalBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalCalFragment extends Fragment {

    private FragmentNormalCalBinding binding;
    private String calculation = "";

    private static Map<String, Integer> operators = new HashMap<>();
    static {
        operators.put("+", 2);
        operators.put("-", 2);
        operators.put("×", 3);
        operators.put("÷", 3);
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
            popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_scientific) {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    Toast.makeText(requireContext(), "已切换到科学计算器", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_history) {
                    Intent intent = new Intent(requireContext(), logs.class);
                    intent.putExtra(logs.EXTRA_IS_SCIENTIFIC, false);
                    requireActivity().startActivity(intent);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void setOnClick(){
        binding.btn0.setOnClickListener(v -> onClickNums("0"));
        binding.btn1.setOnClickListener(v -> onClickNums("1"));
        binding.btn2.setOnClickListener(v -> onClickNums("2"));
        binding.btn3.setOnClickListener(v -> onClickNums("3"));
        binding.btn4.setOnClickListener(v -> onClickNums("4"));
        binding.btn5.setOnClickListener(v -> onClickNums("5"));
        binding.btn6.setOnClickListener(v -> onClickNums("6"));
        binding.btn7.setOnClickListener(v -> onClickNums("7"));
        binding.btn8.setOnClickListener(v -> onClickNums("8"));
        binding.btn9.setOnClickListener(v -> onClickNums("9"));

        binding.btnDiv.setOnClickListener(v -> onClickOps("÷"));
        binding.btnMul.setOnClickListener(v -> onClickOps("×"));
        binding.btnSub.setOnClickListener(v -> onClickOps("-"));
        binding.btnAdd.setOnClickListener(v -> onClickOps("+"));
        binding.btnDot.setOnClickListener(v -> onClickOps("."));
        binding.btnPercent.setOnClickListener(v -> onClickOps("%"));

        binding.btnEqual.setOnClickListener(v -> onClickEqual());
        binding.btnDel.setOnClickListener(v -> onClickDel());
        binding.btnClear.setOnClickListener(v -> onClickC());

        binding.btnMc.setOnClickListener(v -> onClickMs());
        binding.btnMPlus.setOnClickListener(v -> onClickMs());
        binding.btnMMinus.setOnClickListener(v -> onClickMs());
        binding.btnMr.setOnClickListener(v -> onClickMs());
    }

    private void onClickNums(String num){
        calculation += num;
        updateDisplay();
    }

    private void onClickOps(String op){
        if (op.equals(".")) {
            if (!getLastNumber().contains(".")) {
                calculation += op;
            }
        } else if (op.equals("%")) {
            String lastNum = getLastNumber();
            if (!lastNum.isEmpty() && !lastNum.contains("%")) {
                calculation += op;
            }
        } else if (op.equals("-")) {
            if (calculation.isEmpty() || isLastCharOperator()) {
                calculation += op;
            } else {
                replaceLastOperator(op);
            }
        } else {
            replaceLastOperator(op);
        }
        updateDisplay();
    }

    private boolean isLastCharOperator() {
        if (calculation.isEmpty()) return false;
        char last = calculation.charAt(calculation.length() - 1);
        return last == '+' || last == '-' || last == '×' || last == '÷';
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
        int index = -1;
        for (int i = calculation.length() - 1; i >= 0; i--) {
            char c = calculation.charAt(i);
            if (c == '+' || c == '-' || c == '×' || c == '÷') {
                index = i;
                break;
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

    private void onClickEqual(){
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

    private void onClickMs(){
    }

    private void onClickDel(){
        if(calculation.isEmpty()) return;
        calculation = calculation.substring(0, calculation.length() - 1);
        updateDisplay();
    }

    private void onClickC(){
        calculation = "";
        binding.tvExpression.setText("");
        binding.tvResult.setText("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNormalCalBinding.inflate(inflater, container, false);
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