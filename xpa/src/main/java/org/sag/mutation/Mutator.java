package org.sag.mutation;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class Mutator {
    // so far only string and integer are considered.
    String int_function = "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
    String str_function = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
    String int_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only";
    String str_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
    String str_value = "RANDOM$@^$%#&!";
    String str_value1 = "str_A";
    String str_value2 = "str_B";
    String int_value = "-98274365923795632";
    String int_value1 = "123456789";
    String int_value2 = "-987654321";
    private Mutant baseMutant;

    public Mutator(Mutant baseMutant) {
        this.baseMutant = baseMutant;

    }

}
