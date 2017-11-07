package com.picup.calling.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by frank.truong on 4/3/2017.
 */

public class LineNumbers {
    @SerializedName("lineIds")
    @Expose
    private List<String> numbers = null;

    public List<String> getNumbers() {
        return numbers;
    }
}
