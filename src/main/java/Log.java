


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Log {


    public static boolean support_color = true;
    private final static String debug_value_stmt = colorize("value {blu}%s{e} equals {b}{blu}%s");
    private final static String debug_call_stmt = colorize("{b}{r}{u}called{e} !");
    private final static String debug_line_stmt = colorize(" {b}%s{e} ");
    public static Level verbose_level = Level.DEBUG;
    //endregion

    //region coloration
    // ============================================ COLORATION ============================================
    public static int decorator_line_length = 150;

    //endregion


    //region location-extraction
    // ======================================= LOCATION-EXTRACTION ========================================

    static String colorize(String string){
        StringBuilder output = new StringBuilder();
        for (int i=0; i<string.length(); i++){


            if (string.charAt(i) == '{'){
                i++;
                List<String> chars= new ArrayList<>();
                while (i<string.length() && string.charAt(i) != '}'){
                    chars.add(String.valueOf(string.charAt(i)));
                    i++;
                }
                String shrt = chars.stream().collect(Collectors.joining(""));
                if (Style.shortcuts.containsKey(shrt)){
                    output.append(Style.shortcuts.get(shrt).getEscaped());
                }
                else{
                    output.append("{");
                    output.append(shrt);
                    output.append("}");
                }
            }

            else {
                output.append(string.charAt(i));
            }
        }
        output.append(Style.END.getEscaped());
        return output.toString();
    }

    private static CallerInfo get_caller_info(){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return new  CallerInfo(stackTraceElements[3].getClassName(), stackTraceElements[3].getMethodName());
    }

    private static String formated_caller_info(CallerInfo info){
        return colorize(StringUtils.leftPad(String.format("{blu}{b}%s.%s(){e}", info.clss, info.method), 20));
    }

    //endregion

    //region core
    // =============================================== CORE ===============================================

    private static void print(String content, Level level, CallerInfo info, boolean header){

        if (!verbose_level.superiorOrEqualTo(level)){
            return;
        }
        if (header){
            System.out.print(get_header(level, info));
        }
        System.out.println(content);
    }

    private static void print(String content, Level level, CallerInfo info){
        print(content, level, info,true);
    }


    private static String get_header(Level level, CallerInfo info){
        return StringUtils.rightPad(String.format("%s at %s", level.flag, formated_caller_info(info)), 0) + " : " ;
    }
    //endregion
    //region debug
    // ============================================== DEBUG ===============================================

    public static void debug_value(String name, Object value){
        print(String.format(debug_value_stmt, name, value.toString()), Level.DEBUG, get_caller_info());
    }

    public static void debug_call(){
        print(debug_call_stmt, Level.DEBUG, get_caller_info());
    }

    public static void debug_line(String title){
        print(StringUtils.center(
                StringUtils.center(String.format(debug_line_stmt, title), decorator_line_length-10, "-="),
                decorator_line_length, "-"), Level.DEBUG, null, false);
    }


    //region style
    // ============================================== STYLE ===============================================
    enum Style{

        END(0, "e"),

        BOLD(1, "b"),
        UNDERLINED(4, "u"),

        BLACK(30, "blk"),
        RED(31, "r"),
        GREEN(32, "g"),
        YELLOW(33, "y"),
        BLUE(34, "blu"),
        MAGENTA(35, "mgt"),
        WHITE(97, "w");


        public static final String prefix = "\u001B[";
        public static final String suffix = "m";
        static HashMap<String, Style> shortcuts = new HashMap<>();

        static {
            for (Style style : Style.values()){
                shortcuts.put(style.shortcut, style);
            }
        }

        int num;
        String shortcut;

        Style(int num, String shortcut){
            this.num = num;
            this.shortcut = shortcut;
        }

        public String getEscaped(){
            return support_color ? prefix+String.valueOf(this.num)+suffix : "";
        }
    }

    enum Level{

        DEBUG(0, "[{g}{b}DEBUG{e}]"),
        IMPORTANT_DEBUG(25, "{r}{b}DEBUG"),
        INFO(50, "{blu}{b}INFO"),
        WARNING(75, "{y}{b}WARN{e}"),
        ERROR(100, "{b}{r}ERROR"),
        FATAL_ERROR(125, "{b}{r}{u}FATAL");


        public final int level;
        public final String flag;
        Level(int level, String flag){
            this.level = level;
            this.flag=colorize(flag);
        }

        public boolean superiorOrEqualTo(Level level){
            return this.level >= level.level;
        }

    }

    static class CallerInfo{
        public final String clss;
        public final String method;

        CallerInfo(String clss, String method) {
            this.clss = clss;
            this.method = method;
        }
    }

    //endregion






}
