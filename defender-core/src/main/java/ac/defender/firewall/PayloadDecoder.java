// SPDX-License-Identifier: GPL-3.0-only
package ac.defender.firewall;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
/** Bounded, strict protocol decoding. Never stores raw unknown payloads. */
public final class PayloadDecoder {
    private PayloadDecoder() {}
    public static String brand(byte[] data) {
        if (data.length == 0 || data.length > 517) throw new IllegalArgumentException("brand size");
        int length = 0, offset = 0;
        for (; offset < Math.min(data.length, 3); offset++) {
            int b = data[offset] & 255; length |= (b & 127) << (offset * 7);
            if ((b & 128) == 0) {
                offset++;
                if (length != data.length - offset || length > 512) throw new IllegalArgumentException("brand length");
                String value = utf8(Arrays.copyOfRange(data, offset, data.length));
                if (value.length() > 128 || value.codePoints().anyMatch(Character::isISOControl)) throw new IllegalArgumentException("brand text");
                return value;
            }
        }
        throw new IllegalArgumentException("brand varint");
    }
    public static List<String> channels(byte[] data) {
        if (data.length > 8192) throw new IllegalArgumentException("register size");
        String[] values = utf8(data).split("\u0000", -1);
        if (values.length > 128) throw new IllegalArgumentException("register count");
        for (String v : values) if (!v.matches("[a-z0-9_.-]{1,64}:[a-z0-9/._-]{1,64}"))
            throw new IllegalArgumentException("channel syntax");
        return List.of(values);
    }
    private static String utf8(byte[] data) {
        try { return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(data)).toString(); }
        catch (CharacterCodingException e) { throw new IllegalArgumentException("invalid UTF-8"); }
    }
}
