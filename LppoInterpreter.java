import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
public class LppoInterpreter {
    private static final Map<String, Runnable> functions = new HashMap<>();
    private static final Map<String, String> variables = new HashMap<>();
    private static final List<String> scriptLines = new ArrayList<>();
    private String windowTitle = "Default Title";
    private int windowX = 100;
    private int windowY = 100;
    private Color windowColor = Color.WHITE;
    private String windowText = "Hello, World!";
    private Color textColor = Color.BLACK;
    private float textSize = 12f; 
    private int windowWidth = 800;  
    private int windowHeight = 600; 
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java LppoInterpreter <path_to_lppo_file>");
            return;
        }
        String filePath = args[0];
        LppoInterpreter interpreter = new LppoInterpreter();
        String script = "define-function (windowPrint) {\n" +
                        "runner.window.size: 800, 600;\n" +
                        "runner.window.title: \"My First Test\";\n" +
                        "runner.window.position: 100, 100;\n" +
                        "runner.window.color: 255, 255, 255;\n" +
                        "runner.window.text.color: 0, 0, 0;\n" +
                        "runner.window.text.size: 1.2rem;\n" +
                        "runner.window.text: \"Hello, World!\";\n" +
                        "}";
        interpreter.executeScript(script);
        interpreter.loadLppoFile(filePath);
        interpreter.execute();
    }
    public void executeScript(String script) {
        String[] lines = script.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("runner.window")) {
                handleWindowPrint(line);  
            }
        }
    }
    public void loadLppoFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                scriptLines.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void execute() {
        for (int i = 0; i < scriptLines.size(); i++) {
            String line = scriptLines.get(i).trim();
            if (line.isEmpty() || line.startsWith("##") || line.startsWith("-#")) continue;
            if (line.startsWith("sys.start;")) {
                System.out.println("Program started.");
                continue;
            }
            if (line.startsWith("define-function")) {
                i = defineFunction(i);
                continue;
            }
            if (line.startsWith("runner.run")) {
                String functionName = extractFunctionName(line);
                executeFunction(functionName);
                continue;
            }
            if (line.startsWith("sys.pause;")) {
                pause();
                continue;
            }
            if (line.startsWith("sys.end;")) {
                System.out.println("Program has ended.");
                break;
            }
        }
    }
    private int defineFunction(int startIndex) {
        String header = scriptLines.get(startIndex).trim();
        String functionName = extractFunctionName(header);
        StringBuilder functionBody = new StringBuilder();
        int i = startIndex + 1;
        while (i < scriptLines.size()) {
            String line = scriptLines.get(i).trim();
            if (line.equals("}")) break;
            functionBody.append(line).append("\n");
            i++;
        }
        functions.put(functionName, () -> parseFunctionBody(functionBody.toString()));
        return i;
    }
    private void executeFunction(String functionName) {
        Runnable function = functions.get(functionName);
        if (function != null) {
            function.run();
        } else {
            System.err.println("Error: Function not defined: " + functionName);
        }
    }
    private void parseFunctionBody(String body) {
        String[] lines = body.split("\n");
        for (String line : lines) {
            if (line.startsWith("runner.input:")) handleInput(line);
            else if (line.startsWith("runner.terminal:")) handlePrint(line);
            else if (line.contains("<<")) handleVariableAssignment(line);
            else if (line.startsWith("runner.window")) handleWindowPrint(line);
        }
    }
    private void handleInput(String line) {
        String[] parts = line.split("<<");
        String prompt = parts[0].split(":")[1].trim();
        String variableName = parts[1].trim();
        Scanner scanner = new Scanner(System.in);
        System.out.print(prompt + ": ");
        String input = scanner.nextLine();
        variables.put(variableName, input);
    }
    private void handleVariableAssignment(String line) {
        String[] parts = line.split("<<");
        String varName = parts[0].trim();
        String valueExpr = parts[1].split(";")[0].trim();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            valueExpr = valueExpr.replace(entry.getKey(), entry.getValue());
        }
        try {
            double result = evaluateExpression(valueExpr);  
            variables.put(varName, String.valueOf(result));  
        } catch (Exception e) {
            System.err.println("Error evaluating expression: " + valueExpr);
        }
    }
    private double evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");  
        while (expression.contains("^")) {
            int operatorIndex = expression.indexOf("^");
            int leftStart = operatorIndex - 1;
            int rightStart = operatorIndex + 1;
            while (leftStart >= 0 && (Character.isDigit(expression.charAt(leftStart)) || expression.charAt(leftStart) == '.')) {
                leftStart--;
            }
            leftStart++;
            while (rightStart < expression.length() && (Character.isDigit(expression.charAt(rightStart)) || expression.charAt(rightStart) == '.')) {
                rightStart++;
            }
            double leftOperand = Double.parseDouble(expression.substring(leftStart, operatorIndex));
            double rightOperand = Double.parseDouble(expression.substring(operatorIndex + 1, rightStart));
            double intermediateResult = Math.pow(leftOperand, rightOperand);
            expression = expression.substring(0, leftStart) + intermediateResult + expression.substring(rightStart);
        }
        while (expression.contains("*") || expression.contains("/")) {
            int operatorIndex = -1;
            char operator = ' ';
            if (expression.contains("*") && (expression.contains("/") == false || expression.indexOf("*") < expression.indexOf("/"))) {
                operatorIndex = expression.indexOf("*");
                operator = '*';
            } else if (expression.contains("/")) {
                operatorIndex = expression.indexOf("/");
                operator = '/';
            }
            int leftStart = operatorIndex - 1;
            int rightStart = operatorIndex + 1;
            while (leftStart >= 0 && (Character.isDigit(expression.charAt(leftStart)) || expression.charAt(leftStart) == '.')) {
                leftStart--;
            }
            leftStart++;
            while (rightStart < expression.length() && (Character.isDigit(expression.charAt(rightStart)) || expression.charAt(rightStart) == '.')) {
                rightStart++;
            }
            double leftOperand = Double.parseDouble(expression.substring(leftStart, operatorIndex));
            double rightOperand = Double.parseDouble(expression.substring(operatorIndex + 1, rightStart));
            double intermediateResult = operator == '*' ? leftOperand * rightOperand : leftOperand / rightOperand;
            expression = expression.substring(0, leftStart) + intermediateResult + expression.substring(rightStart);
        }
        while (expression.contains("+") || expression.contains("-")) {
            int operatorIndex = -1;
            char operator = ' ';
            if (expression.contains("+") && (expression.contains("-") == false || expression.indexOf("+") < expression.indexOf("-"))) {
                operatorIndex = expression.indexOf("+");
                operator = '+';
            } else if (expression.contains("-")) {
                operatorIndex = expression.indexOf("-");
                operator = '-';
            }
            int leftStart = operatorIndex - 1;
            int rightStart = operatorIndex + 1;
            while (leftStart >= 0 && (Character.isDigit(expression.charAt(leftStart)) || expression.charAt(leftStart) == '.')) {
                leftStart--;
            }
            leftStart++;
            while (rightStart < expression.length() && (Character.isDigit(expression.charAt(rightStart)) || expression.charAt(rightStart) == '.')) {
                rightStart++;
            }
            double leftOperand = Double.parseDouble(expression.substring(leftStart, operatorIndex));
            double rightOperand = Double.parseDouble(expression.substring(operatorIndex + 1, rightStart));
            double intermediateResult = operator == '+' ? leftOperand + rightOperand : leftOperand - rightOperand;
            expression = expression.substring(0, leftStart) + intermediateResult + expression.substring(rightStart);
        }
        return Double.parseDouble(expression);  
    }
    private String replaceVariables(String expression) {
        for (String key : variables.keySet()) {
            expression = expression.replace("<<" + key + ">>", variables.get(key));
        }
        return expression;
    }
    private void handlePrint(String line) {
        String content = line.substring(line.indexOf(":") + 1).trim();
        for (String key : variables.keySet()) {
            content = content.replace("<<" + key + ">>", variables.get(key));
        }
        System.out.println(content);
    }
    private JFrame frame;
    private JPanel panel;
    private JLabel label;
    public void handleWindowPrint(String line) {
        if (line.contains("runner.window.size:")) {
            String size = line.split(":")[1].trim();
            size = size.replace(";", "").trim();
            String[] sizeParts = size.split(",");
            int windowWidth = Integer.parseInt(sizeParts[0].trim());
            int windowHeight = Integer.parseInt(sizeParts[1].trim());
            updateWindowSize(windowWidth, windowHeight);  
        } else if (line.contains("runner.window.title:")) {
            windowTitle = line.split(":")[1].trim();
            updateWindowTitle(windowTitle);  
        } else if (line.contains("runner.window.position:")) {
            String position = line.split(":")[1].trim();
            position = position.replace(";", "").trim();
            String[] positionParts = position.split(",");
            windowX = Integer.parseInt(positionParts[0].trim());
            windowY = Integer.parseInt(positionParts[1].trim());
            updateWindowPosition(windowX, windowY);  
        } else if (line.contains("runner.window.color:")) {
            String color = line.split(":")[1].trim();
            color = color.replace(";", "").trim();
            String[] colorParts = color.split(",");
            windowColor = new Color(Integer.parseInt(colorParts[0].trim()),
                                    Integer.parseInt(colorParts[1].trim()),
                                    Integer.parseInt(colorParts[2].trim()));
            updateWindowColor(windowColor);  
        } else if (line.contains("runner.window.text.color:")) {
            String color = line.split(":")[1].trim();
            color = color.replace(";", "").trim();
            String[] colorParts = color.split(",");
            textColor = new Color(Integer.parseInt(colorParts[0].trim()),
                                  Integer.parseInt(colorParts[1].trim()),
                                  Integer.parseInt(colorParts[2].trim()));
            updateTextColor(textColor);  
        } else if (line.contains("runner.window.text.size:")) {
            String size = line.split(":")[1].trim();
            size = size.replace(";", "").trim();
            textSize = Float.parseFloat(size.replace("rem", "").trim());
            updateTextSize(textSize);  
        } else if (line.contains("runner.window.text:")) {
            windowText = line.split(":")[1].trim();
            updateWindowText(windowText);  
        }
    }
    private void updateWindowSize(int width, int height) {
        if (frame != null) {
            frame.setSize(width, height);
        }
    }
    private void updateWindowTitle(String title) {
        if (frame != null) {
            frame.setTitle(title);
        }
    }
    private void updateWindowPosition(int x, int y) {
        if (frame != null) {
            frame.setLocation(x, y);
        }
    }
    private void updateWindowColor(Color color) {
        if (frame != null) {
            frame.getContentPane().setBackground(color);
        }
    }
    private void updateTextColor(Color color) {
        if (label != null) {
            label.setForeground(color);
        }
    }
    private void updateTextSize(float size) {
        if (label != null) {
            label.setFont(new Font("Arial", Font.PLAIN, (int) size));
        }
    }
    private void updateWindowText(String text) {
        if (label != null) {
            label.setText(text);
        }
    }
    public void createWindow() {
        frame = new JFrame(windowTitle);
        frame.setSize(800, 600);
        frame.setLocation(windowX, windowY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(windowColor);
        label = new JLabel(windowText);
        label.setFont(new Font("Arial", Font.PLAIN, (int) textSize));
        label.setForeground(textColor);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        frame.getContentPane().add(label);
        frame.setVisible(true);
    }
    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        if (op == '^') return 3;
        return -1;
    }
    private double applyOperation(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return a / b;
            case '^': return Math.pow(a, b);
        }
        return 0;
    }
    private String extractFunctionName(String line) {
        return line.split("\\(")[1].split("\\)")[0].trim();
    }
    private void pause() {
        System.out.println("Execution paused. Press Enter to continue...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
