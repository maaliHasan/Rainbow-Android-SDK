package com.ale.util;


import com.ale.util.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by georges on 05/09/2016.
 */
public class SmileyMgr {
    private static final String LOG_TAG = "SmileyMgr";

    private static class SmileyInfos {
        private String escapedStrg;
        private int unicodeValue;
        public SmileyInfos(String escSmiley,int uniVal) {
            this.escapedStrg = escSmiley;
            this.unicodeValue = uniVal;
        }

        public String getEscapedStrg() {
            return escapedStrg;
        }

        public int getUnicodeValue() {
            return unicodeValue;
        }
    }

    // \.[]{}()*+-?^$|
    private final List<String> m_smileyKeys;
    private static Map m_smileys= new HashMap<String, SmileyInfos>(){{
        put("<3",   new SmileyInfos("<3",0x2764));
        put("</3",  new SmileyInfos("</3",0x1f494));
        put(":\')", new SmileyInfos(":\'\\)",0x1f602));
        put(":\'-)",new SmileyInfos(":\'-\\)",0x1f602));
        put(":D",   new SmileyInfos(":D",0x1f603));
        put(":-D",  new SmileyInfos(":-D",0x1f603));
        put("=D",   new SmileyInfos("=D",0x1f603));
        put(":)",   new SmileyInfos(":\\)",0x1f604));
        put(":-)",  new SmileyInfos(":-\\)",0x1f604));
        put("=]",   new SmileyInfos("=]",0x1f604));
        put("=)",   new SmileyInfos("=\\)",0x1f604));
        put(":]",   new SmileyInfos(":\\]",0x1f604));
        put("\':)", new SmileyInfos("\':\\)",0x1f605));
        put("\':-)",new SmileyInfos("\':-\\)",0x1f605));
        put("\'=)", new SmileyInfos("\'=\\)",0x1f605));
        put("\':D", new SmileyInfos("\':D",0x1f605));
        put("\':-D",new SmileyInfos("\':-D",0x1f605));
        put("\'=D", new SmileyInfos("\'=D",0x1f605));
        put(">:)",  new SmileyInfos(">:\\)",0x1f606));
        put(">;)",  new SmileyInfos(">;\\)",0x1f606));
        put(">:-)", new SmileyInfos(">:-\\)",0x1f606));
        put(">=)",  new SmileyInfos(">=\\)",0x1f606));
        put(";)",   new SmileyInfos(";\\)",0x1f609));
        put(";-)",  new SmileyInfos(";-\\)",0x1f609));
        put("*-)",  new SmileyInfos("\\*-\\)",0x1f609));
        put("*)",   new SmileyInfos("\\*\\)",0x1f609));
        put(";-]",  new SmileyInfos(";-\\]",0x1f609));
        put(";]",   new SmileyInfos(";\\]",0x1f609));
        put(";D",   new SmileyInfos(";D",0x1f609));
        put(";^)",  new SmileyInfos(";\\^\\)",0x1f609));
        put("\':(", new SmileyInfos("\':\\(",0x1f613));
        put("\':-(",new SmileyInfos("\':-\\(",0x1f613));
        put("\'=(", new SmileyInfos("\'=\\(",0x1f613));
        put(":*",   new SmileyInfos(":\\*",0x1f618));
        put(":-*",  new SmileyInfos(":-\\*",0x1f618));
        put("=*",   new SmileyInfos("=\\*",0x1f618));
        put(":^*",  new SmileyInfos(":\\^\\*",0x1f618));
        put(">:P",  new SmileyInfos(">:P",0x1f61c));
        put("X-P",  new SmileyInfos("X-P",0x1f61c));
        put("x-p",  new SmileyInfos("x-p",0x1f61c));
        put(">:[",  new SmileyInfos(">:\\[",0x1f61e));
        put(":-(",  new SmileyInfos(":-\\(",0x1f61e));
        put(":(",   new SmileyInfos(":\\(",0x1f61e));
        put(":-[",  new SmileyInfos(":-\\[",0x1f61e));
        put(":[",   new SmileyInfos(":\\[",0x1f61e));
        put("=(",   new SmileyInfos("=\\(",0x1f61e));
        put(">:(",  new SmileyInfos(">:\\(",0x1f620));
        put(">:-(", new SmileyInfos(">:-\\(",0x1f620));
        put(":@",   new SmileyInfos(":@",0x1f620));
        put(":\'(", new SmileyInfos(":\'\\(",0x1f622));
        put(":\'-(",new SmileyInfos(":\'-\\(",0x1f622));
        put(";(",   new SmileyInfos(";\\(",0x1f622));
        put(";-(",  new SmileyInfos(";-\\(",0x1f622));
        put(">.<",  new SmileyInfos(">.<",0x1f623));
        put("D:",   new SmileyInfos("D:",0x1f628));
        put(":$",   new SmileyInfos(":\\$",0x1f633));
        put("=$",   new SmileyInfos("=\\$",0x1f633));
        put("#-)",  new SmileyInfos("#-\\)",0x1f635));
        put("#)",   new SmileyInfos("#\\)",0x1f635));
        put("%-)",  new SmileyInfos("%-\\)",0x1f635));
        put("%)",   new SmileyInfos("%\\)",0x1f635));
        put("X)",   new SmileyInfos("X\\)",0x1f635));
        put("X-)",  new SmileyInfos("X-\\)",0x1f635));
        put("*\\0/*",new SmileyInfos("\\*\\\\0\\/\\*",0x1f646));
        put("\\0/", new SmileyInfos("\\\\0/",0x1f646));
        put("*\\O/*",new SmileyInfos("\\*\\\\O\\/\\*",0x1f646));
        put("\\O/", new SmileyInfos("\\\\O/",0x1f646));
        put("O:-)", new SmileyInfos("O:-\\)",0x1f607));
        put("0:-3", new SmileyInfos("0:-3",0x1f607));
        put("0:3",  new SmileyInfos("0:3",0x1f607));
        put("0:-)", new SmileyInfos("0:-\\)",0x1f607));
        put("0:)",  new SmileyInfos("0:\\)",0x1f607));
        put("0;^)", new SmileyInfos("0;\\^\\)",0x1f607));
        put("O:)",  new SmileyInfos("O:\\)",0x1f607));
        put("O;-)", new SmileyInfos("O;-\\)",0x1f607));
        put("O=)",  new SmileyInfos("O=\\)",0x1f607));
        put("0;-)", new SmileyInfos("0;-\\)",0x1f607));
        put("O:-3", new SmileyInfos("O:-3",0x1f607));
        put("O:3",  new SmileyInfos("O:3",0x1f607));
        put("B-)",  new SmileyInfos("B-\\)",0x1f60e));
        put("B)",   new SmileyInfos("B\\)",0x1f60e));
        put("8)",   new SmileyInfos("8\\)",0x1f60e));
        put("8-)",  new SmileyInfos("8-\\)",0x1f60e));
        put("B-D",  new SmileyInfos("B-D",0x1f60e));
        put("8-D",  new SmileyInfos("8-D",0x1f60e));
        put("-_-",  new SmileyInfos("-_-",0x1f611));
        put("-__-", new SmileyInfos("-__-",0x1f611));
        put("-___-",new SmileyInfos("-___-",0x1f611));
        put(">:\\", new SmileyInfos(">:\\\\",0x1f615));
        put(">:/",  new SmileyInfos(">:/",0x1f615));
        put(":-/",  new SmileyInfos(":-/",0x1f615));
        put(":-.",  new SmileyInfos(":-.",0x1f615));
        put(":/",   new SmileyInfos(":/",0x1f615));
        put(":\\",  new SmileyInfos(":\\\\",0x1f615));
        put("=/",   new SmileyInfos("=/",0x1f615));
        put("=\\",  new SmileyInfos("=\\\\",0x1f615));
        put(":L",   new SmileyInfos(":L",0x1f615));
        put("=L",   new SmileyInfos("=L",0x1f615));
        put(":P",   new SmileyInfos(":P",0x1f61b));
        put(":-P",  new SmileyInfos(":-P",0x1f61b));
        put("=P",   new SmileyInfos("=P",0x1f61b));
        put(":-p",  new SmileyInfos(":-p",0x1f61b));
        put(":p",   new SmileyInfos(":p",0x1f61b));
        put("=p",   new SmileyInfos("=p",0x1f61b));
        put(":-Þ",  new SmileyInfos(":-Þ",0x1f61b));
        put(":Þ",   new SmileyInfos(":Þ",0x1f61b));
        put(":þ",   new SmileyInfos(":þ",0x1f61b));
        put(":-þ",  new SmileyInfos(":-þ",0x1f61b));
        put(":-b",  new SmileyInfos(":-b",0x1f61b));
        put(":b",   new SmileyInfos(":b",0x1f61b));
        put("d:",   new SmileyInfos("d:",0x1f61b));
        put(":-O",  new SmileyInfos(":-O",0x1f62e));
        put(":O",   new SmileyInfos(":O",0x1f62e));
        put(":-o",  new SmileyInfos(":-o",0x1f62e));
        put(":o",   new SmileyInfos(":o",0x1f62e));
        put("O_O",  new SmileyInfos("O_O",0x1f62e));
        put(">:O",  new SmileyInfos(">:O",0x1f62e));
        put(":-X",  new SmileyInfos(":-X",0x1f636));
        put(":X",   new SmileyInfos(":X",0x1f636));
        put(":-#",  new SmileyInfos(":-#",0x1f636));
        put(":#",   new SmileyInfos(":#",0x1f636));
        put("=X",   new SmileyInfos("=X",0x1f636));
        put("=x",   new SmileyInfos("=x",0x1f636));
        put(":x",   new SmileyInfos(":x",0x1f636));
        put(":-x",  new SmileyInfos(":-x",0x1f636));
        put("=#",   new SmileyInfos("=#",0x1f636));
    }};

    public SmileyMgr() {
        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String s1, String s2) {
                if( s1 == null)
                    return -1;
                if( s2 == null)
                    return 1;

                return s2.length() - s1.length();
            }
        };
        m_smileyKeys = new ArrayList<>();
        m_smileyKeys.addAll(m_smileys.keySet());
        Collections.sort(m_smileyKeys, comparator);
    }

    public String convertSmileyString(String input) {
        if( input == null ) {
            return "";
        }

        String textSmileyConverted = input;
        SmileyInfos smileyInfos;
        String escapedStrg;
        int smileyUnicode;
        String unicodeStrg;

        for(String smileyStrg : m_smileyKeys) {

            smileyInfos = (SmileyInfos) m_smileys.get(smileyStrg);

            if (smileyInfos != null) {

                // Get the smiley index into message input string
                int smileyIndex = textSmileyConverted.indexOf(smileyStrg);

                // Contains a smiley
                if (smileyIndex > -1) {

                    // We allow the emoticon if :
                    // - is first-char of the string OR previous char is a whitespace
                    if (smileyIndex == 0 || Character.isWhitespace(textSmileyConverted.charAt(smileyIndex - 1)))
                    {
                        // AND
                        // - is last-char of the string OR next char is a (whitespace , . ? ! \n)
                        if ( ((smileyIndex + smileyStrg.length()) <= textSmileyConverted.length()) ||
                                String.valueOf(textSmileyConverted.charAt(smileyIndex + smileyStrg.length())).matches("[.,?! \n]"))
                        {
                            escapedStrg = smileyInfos.getEscapedStrg();
                            smileyUnicode = smileyInfos.getUnicodeValue();
                            unicodeStrg = getUnicodeStrg(smileyUnicode);
                            textSmileyConverted = textSmileyConverted.replaceAll(escapedStrg, unicodeStrg);
                        }
                    }
                }
            }
        }
        return textSmileyConverted;
    }

    public static String getSmileyUnicode(String smiley) {

        Iterator entries = m_smileys.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            String key = (String) thisEntry.getKey();
            SmileyInfos smileyInfos = (SmileyInfos) thisEntry.getValue();

            if(smiley.contains(key)) {
                int smileyUnicode = smileyInfos.getUnicodeValue();

                smiley = getUnicodeStrg(smileyUnicode);
            }
        }
        return smiley;
    }

    private static String getUnicodeStrg(int smileyUnicode) {
        char[] converttoBytes = Character.toChars(smileyUnicode);
        return( String.valueOf(converttoBytes) );
    }

}
