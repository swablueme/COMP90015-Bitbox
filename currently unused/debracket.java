package unimelb.bitbox;

import java.util.*;
import java.util.stream.Collectors;


public class debracket {

    public static ArrayList<Integer> fixmylist(String value) {
        System.out.println("CONVERTING");
        String debracketed = value.replace("[", "").replace("]", "");
        String trimmed = debracketed.replaceAll("\\s+", "");
        ArrayList<String> trimmedlist = new ArrayList<String>(Arrays.asList(trimmed.split(",")));

        ArrayList<Integer> list = trimmedlist.stream()
                .map(s -> Integer.valueOf(s))
                .collect(Collectors.toCollection(ArrayList::new));
        return list;
    }
}
