/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.utils;

import java.util.Arrays;

public final class ArrayUtils<T> {

    /**
     *
     * @param <T>
     * @param list
     * @param object
     * @return
     */
    public static <T> int find(final T[] list, final T object) {
        Arrays.sort(list);
        return Arrays.binarySearch(list, object);
    }

}
