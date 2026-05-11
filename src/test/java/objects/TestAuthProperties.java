package objects;

import com.marketpilot.util.BufferedConverter;

public class TestAuthProperties {
    private TestAuthProperties() {}

    public static byte[] dummyPasswordHash() {
        return BufferedConverter.toBytes("xcusdhfgvasj@#njkhbf@nmdsejkhf%jnkjkbhjsd!!@$%bn1sdasd2n19xvds71ns3");
    }

    public static String totpSecret() {
        return "4r3e2w1q";
    }
}
