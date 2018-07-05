package de.hof.university.app.chat.Helper;

public class StringTrimmer {

    public String trimmTill(String str, char chr) {

        int index = str.indexOf(chr);
        return str.substring(0, index);

    }

    public String trimmStartingAt(String str, char chr){
        int index = str.indexOf(chr);
        return str.substring(index + 1);

    }
}
