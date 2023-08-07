package icu.xmc.edgettslib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import timber.log.Timber;

public class TTSWebSocketListener extends WebSocketListener {

    private static final byte[] head = new byte[]{0x50, 0x61, 0x74, 0x68, 0x3a, 0x61, 0x75, 0x64, 0x69, 0x6f, 0x0d, 0x0a};

    private String storage;
    private String fileName;
    private Boolean findHeadHook;

    public TTSWebSocketListener(String storage, String fileName, Boolean findHeadHook) {
        this.storage = storage;
        this.fileName = fileName;
        this.findHeadHook = findHeadHook;
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
        Timber.d("TTSWebSocket onClosed");
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        Timber.d("TTSWebSocket onFailure");
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        Timber.d("TTSWebSocket onMessage String");
        if (text.contains("Path:turn.end")) {
            webSocket.close(1000,null);
        }
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        Timber.d("TTSWebSocket onMessage ByteString");
        if (findHeadHook) {
            findHeadHook(bytes.toByteArray());
        } else {
            fixHeadHook(bytes.toByteArray());
        }
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        Timber.d("TTSWebSocket onOpen");
    }

    /**
     * This implementation method is more generic as it searches for the file header marker in the given file header and removes it. However, it may have lower efficiency.
     *
     * @param origin
     */
    private void findHeadHook(byte[] origin) {
        int headIndex = -1;
        for (int i = 0; i < origin.length - head.length; i++) {
            boolean match = true;
            for (int j = 0; j < head.length; j++) {
                if (origin[i + j] != head[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                headIndex = i;
                break;
            }
        }
        if (headIndex != -1) {
            byte[] voiceBytesRemoveHead = Arrays.copyOfRange(origin, headIndex + head.length, origin.length);

            try (FileOutputStream fos = new FileOutputStream(storage + File.separator + fileName, true)) {
                fos.write(voiceBytesRemoveHead);
                fos.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method directly specifies the file header marker, which makes it faster. However, if the format changes, it may become unusable.
     *
     * @param origin
     */
    public void fixHeadHook(byte[] origin) {
        String str = new String(origin);
        int skip;
        if (str.contains("Content-Type")) {
            if (str.contains("audio/mpeg")) {
                skip = 130;
            } else if (str.contains("codec=opus")) {
                skip = 142;
            } else {
                skip = 0;
            }
        } else {
            skip = 105;
        }
        byte[] voiceBytesRemoveHead = Arrays.copyOfRange(origin, skip, origin.length);
        try (FileOutputStream fos = new FileOutputStream(storage + File.separator + fileName, true)) {
            fos.write(voiceBytesRemoveHead);
            fos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
