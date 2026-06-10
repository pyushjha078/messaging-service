package com.messaging.service.support;

import java.util.Base64;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Cursor {

    // Encode: wrap the message id in Base64URL so clients treat it as opaque
    public static String encode(long messageId) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(Long.toString(messageId).getBytes(UTF_8));
    }

    // Decode: reverse the above. Throw 400 if the string is malformed.
    public static long decode(String cursor) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            return Long.parseLong(new String(bytes, UTF_8));
        } catch (Exception e) {
            throw new InvalidCursorException("Cursor is not valid: " + cursor);
        }
    }
}
