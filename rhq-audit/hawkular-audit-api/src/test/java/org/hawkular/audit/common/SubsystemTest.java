package org.hawkular.audit.common;

import org.junit.Assert;
import org.junit.Test;

public class SubsystemTest {
    @Test
    public void testEquality() {
        Assert.assertEquals(Subsystem.MISCELLANEOUS, Subsystem.MISCELLANEOUS);
        Assert.assertEquals(new Subsystem("foo"), new Subsystem("foo"));
        Assert.assertFalse(new Subsystem("foo").equals(new Subsystem("bar")));
    }
}
