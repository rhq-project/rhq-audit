package org.rhq.idgen.common;

import java.util.UUID;

import org.testng.annotations.Test;

@Test
public class UUIDGeneratorTest {

    public void testUUID() {
        UUID namespace = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        UUID uuid1 = UUIDGenerator.generateUuidV5("this-is-a-test-of-uuid-gen", namespace);
        assert uuid1 != null;
        UUID uuid2 = UUIDGenerator.generateUuidV5("this-is-a-test-of-uuid-gen", namespace);
        assert uuid2 != null;
        assert uuid1.equals(uuid2) : "uuid1 [" + uuid1 + "] != uuid2 [" + uuid2 + "]";
        assert uuid1.toString().equals("37728d03-892d-51d5-bc67-850785d38e1b");
    }
}
