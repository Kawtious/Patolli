/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.utils;

public final class ValidationUtils {

    /**
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    /**
     * Function to validate hexadecimal color code
     *
     * @param str
     * @return
     */
    public static boolean isValidHexaCode(String str) {
        // Regex to check valid hexadecimal color code.
        return str.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }

    /**
     *
     * @param input
     * @return
     */
    public static boolean validateCommand(String input) {
        return input.substring(0, 1).equals("/");
    }

    private ValidationUtils() {
    }
}
