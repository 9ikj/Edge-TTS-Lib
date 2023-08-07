package icu.xmc.edgettslib;

public class TTSUtil {
    public static String mkAudioFormat(String dateStr, String format) {
        return "X-Timestamp:" + dateStr + "\r\n" +
                "Content-Type:application/json; charset=utf-8\r\n" +
                "Path:speech.config\r\n\r\n" +
                "{\"context\":{\"synthesis\":{\"audio\":{\"metadataoptions\":{\"sentenceBoundaryEnabled\":\"false\",\"wordBoundaryEnabled\":\"true\"},\"outputFormat\":\"" + format + "\"}}}}\n";
    }


    public static String mkssml(String locate, String voiceName, String content, String voicePitch, String voiceRate, String voiceVolume) {
        return "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='" + locate + "'>" +
                "<voice name='" + voiceName + "'><prosody pitch='" + voicePitch + "' rate='" + voiceRate + "' volume='" + voiceVolume + "'>" +
                content + "</prosody></voice></speak>";
    }


    public static String ssmlHeadersPlusData(String requestId, String timestamp, String ssml) {
        return "X-RequestId:" + requestId + "\r\n" +
                "Content-Type:application/ssml+xml\r\n" +
                "X-Timestamp:" + timestamp + "Z\r\n" +
                "Path:ssml\r\n\r\n" + ssml;
    }

}
