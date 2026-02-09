package com.example.calculator.controller;

import com.example.calculator.model.CalcRequest;
import com.example.calculator.model.CalcResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CalculatorController {

    @PostMapping("/calc")
    public ResponseEntity<CalcResponse> calculate(@RequestBody CalcRequest request) {
        if (request.getOperation() == null || request.getA() == null) {
            return ResponseEntity.badRequest().body(new CalcResponse(Double.NaN, "Operation and a are required."));
        }

        double a = request.getA();
        Double bValue = request.getB();
        String op = request.getOperation().trim().toLowerCase();

        try {
            double result;
            switch (op) {
                case "add":
                    result = a + requireB(bValue);
                    break;
                case "subtract":
                    result = a - requireB(bValue);
                    break;
                case "multiply":
                    result = a * requireB(bValue);
                    break;
                case "divide":
                    double divisor = requireB(bValue);
                    if (divisor == 0.0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new CalcResponse(Double.NaN, "Division by zero."));
                    }
                    result = a / divisor;
                    break;
                case "power":
                    result = Math.pow(a, requireB(bValue));
                    break;
                case "sqrt":
                    if (a < 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new CalcResponse(Double.NaN, "Square root requires non-negative a."));
                    }
                    result = Math.sqrt(a);
                    break;
                case "sin":
                    result = Math.sin(Math.toRadians(a));
                    break;
                case "cos":
                    result = Math.cos(Math.toRadians(a));
                    break;
                case "tan":
                    result = Math.tan(Math.toRadians(a));
                    break;
                case "log10":
                    if (a <= 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new CalcResponse(Double.NaN, "Log10 requires positive a."));
                    }
                    result = Math.log10(a);
                    break;
                case "ln":
                    if (a <= 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new CalcResponse(Double.NaN, "Ln requires positive a."));
                    }
                    result = Math.log(a);
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new CalcResponse(Double.NaN, "Unsupported operation."));
            }

            return ResponseEntity.ok(new CalcResponse(result, "OK"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CalcResponse(Double.NaN, ex.getMessage()));
        }
    }

    private double requireB(Double bValue) {
        if (bValue == null) {
            throw new IllegalArgumentException("Operation requires b.");
        }
        return bValue;
    }
}
