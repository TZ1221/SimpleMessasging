package edu.northeastern.ccs.im;

//'main' method must be in a class 'Rextester'.
//Compiler version 1.8.0_111

import java.util.*;
import java.lang.*;

class Rextester
{

    public static int binarySearch(String[] a, String x) {
        int low = 0;
        int high = a.length - 1;
        int mid;

        while (low <= high) {
            mid = (low + high) / 2;

            if (a[mid].compareTo(x) < 0) {
                low = mid + 1;
            } else if (a[mid].compareTo(x) > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return -1;
    }


    public static void main(String[] args) {

        String str = "This is example message with bad words like FWORD ";
        String[] text = str.split(" ");
        String search ="example like with is haha" ;
        String[] badwords = search.split(" ");

        Arrays.sort( text);



        for (String badword :badwords){
            int searchIndex = binarySearch(text,badword);

            System.out.println(searchIndex != -1 ? text[searchIndex]+ " - Index is "+searchIndex : "Not found");



        }


    }



    public static void example()
    {
        String badWords = "FWORD example";
        String str = "This is example message with bad words like FWORD ";
        String[] words = str.split(" ");


        for (String word :words){
            if ( badWords.contains (word)   )
            {
                System.out.println(word);
            }
        }



    }
}