package com.ndash.identity_framework.services;

import com.ctc.wstx.util.StringUtil;

import java.util.Arrays;

public class Test {

    public static void main(String[] args) {
        String str = "abcba";
        System.out.println(isPlaindrome(str.toCharArray()));

    }

    public static boolean isPlaindrome(char[] str){
        int j = str.length-1;
        for(int i=0; i< str.length/2 ;i++){
            if(str[i] != str[j]){
                return false;
            }
            j--;
        }
        return true;
    }
}
