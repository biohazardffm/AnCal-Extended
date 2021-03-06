package pl.magot.vetch.widgets;

public class dayStyle {
    public final static int iColorFrameHeader = 0xff666666;
    public final static int iColorFrameHeaderHoliday = 0xff707070;
    public final static int iColorTextHeader = 0xffcccccc;
    public final static int iColorTextHeaderHoliday = 0xffd0d0d0;

    public final static int iColorText = 0xffdddddd;
    public final static int iColorBkg = 0xff888888;
    public final static int iColorTextHoliday = 0xfff0f0f0;
    public final static int iColorBkgHoliday = 0xffaaaaaa;

    public final static int iColorTextToday = 0xff002200;
    public final static int iColorBkgToday = 0xff88bb88;

    public final static int iColorTextSelected = 0xff001122;
    public final static int iColorBkgSelectedLight = 0xffbbddff;
    public final static int iColorBkgSelectedDark = 0xff225599;

    public final static int iColorTextFocused = 0xff221100;
    public final static int iColorBkgFocusLight = 0xffffddbb;
    public final static int iColorBkgFocusDark = 0xffaa5500;


    public static int getColorFrameHeader(boolean bHoliday) {
        if (bHoliday)
            return iColorFrameHeaderHoliday;
        return iColorFrameHeader;
    }

    public static int getColorTextHeader(boolean bHoliday) {
        if (bHoliday)
            return iColorTextHeaderHoliday;
        return iColorTextHeader;
    }

    public static int getColorText(boolean bHoliday, boolean bToday) {
        if (bToday)
            return iColorTextToday;
        if (bHoliday)
            return iColorTextHoliday;
        return iColorText;
    }

    public static int getColorBkg(boolean bHoliday, boolean bToday) {
        if (bToday)
            return iColorBkgToday;
        if (bHoliday)
            return iColorBkgHoliday;
        return iColorBkg;
    }
}
