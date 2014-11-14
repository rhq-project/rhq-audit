package org.rhq.idgen.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDGenerator {
    /**
     * Generates a version 5 UUID.
     * 
     * @param name
     *            the name of the resource
     * @param namespace
     *            the namespace of the name
     * @return the v5 UUID
     */
    public static UUID generateUuidV5(String name, UUID namespace) {
        // per spec, append the name bytes to the namespace bytes - we'll hash those combined bytes
        final byte[] nameAsBytes = name.getBytes();
        final byte[] namespaceAsBytes = getUuidRawBytes(namespace);
        byte[] concat = new byte[namespaceAsBytes.length + nameAsBytes.length];
        System.arraycopy(namespaceAsBytes, 0, concat, 0, namespaceAsBytes.length);
        System.arraycopy(nameAsBytes, 0, concat, namespaceAsBytes.length, nameAsBytes.length);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("SHA-1 not supported"); // all java impls are supposed to support this
        }

        byte[] shaDigest = md.digest(concat); // SHA-1 returns a 20-byte digest ...
        byte[] hashBytes = new byte[16]; // ... but we can only use 16.
        System.arraycopy(shaDigest, 0, hashBytes, 0, 16);

        hashBytes[6] &= 0x0f; /* clear version */
        hashBytes[6] |= 0x50; /* set to version 5 */
        hashBytes[8] &= 0x3f; /* clear variant */
        hashBytes[8] |= 0x80; /* set to IETF variant */
        long[] msbLsb = getMsbLsb(hashBytes);
        return new UUID(msbLsb[0], msbLsb[1]);
    }

    private static byte[] getUuidRawBytes(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] rawBytes = new byte[16];
        rawBytes[0] = (byte) (msb >>> 56);
        rawBytes[1] = (byte) (msb >>> 48);
        rawBytes[2] = (byte) (msb >>> 40);
        rawBytes[3] = (byte) (msb >>> 32);
        rawBytes[4] = (byte) (msb >>> 24);
        rawBytes[5] = (byte) (msb >>> 16);
        rawBytes[6] = (byte) (msb >>> 8);
        rawBytes[7] = (byte) (msb >>> 0);
        rawBytes[8] = (byte) (lsb >>> 56);
        rawBytes[9] = (byte) (lsb >>> 48);
        rawBytes[10] = (byte) (lsb >>> 40);
        rawBytes[11] = (byte) (lsb >>> 32);
        rawBytes[12] = (byte) (lsb >>> 24);
        rawBytes[13] = (byte) (lsb >>> 16);
        rawBytes[14] = (byte) (lsb >>> 8);
        rawBytes[15] = (byte) (lsb >>> 0);
        return rawBytes;
    }

    private static long[] getMsbLsb(byte[] uuidBytes) {
        if (uuidBytes == null) {
            throw new NullPointerException("UUID byte array must not be null");
        }
        if (uuidBytes.length != 16) {
            throw new IllegalArgumentException("UUID byte array must be 16 bytes in length not [" + uuidBytes.length + "]");
        }
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (uuidBytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (uuidBytes[i] & 0xff);
        }
        return new long[] { msb, lsb };
    }

}
