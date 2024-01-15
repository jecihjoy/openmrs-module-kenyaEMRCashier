package org.openmrs.module.kenyaemr.cashier.rest.restmapper;

import org.apache.logging.log4j.util.Strings;

public class TestMain {

    public static void main(String[] args) {
        String val = "  Clinical consultation-167410     ";
        String[] values = val.split("-");

//        System.out.println("hello world "+ values[0]);
//        System.out.println("hello world "+ val.split("-")[1]);
        System.out.println("hello world"+ val.trim() +"here");
        System.out.println("hello world"+ val + "here");
    }
}


