package org.howard.edu.lsp.assignment2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;

public class ETLPipeline {

    public static void main(String[] args) throws IOException {

        // Input and output file paths
        Path inputPath = Path.of("data/employees.csv");
        Path outputPath = Path.of("data/transformed_employees.csv");

        // Counters
        int rowsRead = 0;
        int rowsTransformed = 0;
        int rowsSkipped = 0;

        // Open input and output files
        BufferedReader reader = Files.newBufferedReader(inputPath);
        BufferedWriter writer = Files.newBufferedWriter(outputPath);

        // Write the required output header
        writer.write(
                "EmployeeID,Name,Department,HoursWorked,HourlyRate,"
                        + "GrossPay,PayLevel,EmploymentStatus"
        );
        writer.newLine();

        // Skip the header in the input file
        reader.readLine();

        String line;

        // Read every remaining row
        while ((line = reader.readLine()) != null) {

            // Every non-header line counts as a row read
            rowsRead++;

            // Skip blank rows
            if (line.trim().isEmpty()) {
                rowsSkipped++;
                continue;
            }

            // Split into fields
            String[] fields = line.split(",", -1);

            // Each row must contain exactly 5 fields
            if (fields.length != 5) {
                rowsSkipped++;
                continue;
            }

            int employeeId;
            String name;
            String department;
            double hoursWorked;
            double hourlyRate;

            // Parse and clean the fields
            try {
                employeeId = Integer.parseInt(fields[0].trim());
                name = fields[1].trim().toUpperCase();
                department = fields[2].trim();
                hoursWorked = Double.parseDouble(fields[3].trim());
                hourlyRate = Double.parseDouble(fields[4].trim());

            } catch (NumberFormatException e) {
                // Invalid numeric data means the row is skipped
                rowsSkipped++;
                continue;
            }

            // HoursWorked and HourlyRate cannot be negative
            if (hoursWorked < 0 || hourlyRate < 0) {
                rowsSkipped++;
                continue;
            }

            // Calculate gross pay
            double grossPay;

            if (hoursWorked <= 40) {

                // All hours are regular hours
                grossPay = hoursWorked * hourlyRate;

            } else {

                // First 40 hours are regular
                double regularPay = 40 * hourlyRate;

                // Hours above 40 are overtime
                double overtimeHours = hoursWorked - 40;

                // Overtime is paid at 1.5 times the hourly rate
                double overtimePay = overtimeHours * hourlyRate * 1.5;

                grossPay = regularPay + overtimePay;
            }

            // IT employees receive a 5% bonus
            if (department.equals("IT")) {
                grossPay = grossPay * 1.05;
            }

            // Round GrossPay to exactly 2 decimal places using HALF_UP
            BigDecimal roundedGrossPay = BigDecimal.valueOf(grossPay)
                    .setScale(2, RoundingMode.HALF_UP);

            // Determine PayLevel using the final rounded GrossPay
            String payLevel;

            if (roundedGrossPay.compareTo(new BigDecimal("500")) < 0) {
                payLevel = "Low";

            } else if (roundedGrossPay.compareTo(new BigDecimal("1000")) < 0) {
                payLevel = "Standard";

            } else if (roundedGrossPay.compareTo(new BigDecimal("2000")) < 0) {
                payLevel = "High";

            } else {
                payLevel = "Executive";
            }

            // Determine EmploymentStatus
            String employmentStatus;

            if (hoursWorked < 30) {
                employmentStatus = "Part-Time";
            } else {
                employmentStatus = "Full-Time";
            }

            // Write the transformed employee to the output CSV
            writer.write(
                    employeeId + ","
                            + name + ","
                            + department + ","
                            + String.format("%.2f", hoursWorked) + ","
                            + String.format("%.2f", hourlyRate) + ","
                            + roundedGrossPay.toPlainString() + ","
                            + payLevel + ","
                            + employmentStatus
            );

            writer.newLine();

            // Count successfully transformed rows
            rowsTransformed++;
        }

        // Close the files
        reader.close();
        writer.close();

        // Print the required summary
        System.out.println("Rows read: " + rowsRead);
        System.out.println("Rows transformed: " + rowsTransformed);
        System.out.println("Rows skipped: " + rowsSkipped);
        System.out.println("Output file: data/transformed_employees.csv");
    }
}