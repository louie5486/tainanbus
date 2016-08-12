// IMyAidlInterface.aidl
package tw.com.hantek.www.ota;

// Declare any non-default types here with import statements

interface IRemoteService {
    /** Request the process ID of this service, to do evil things with it. */
    int getPid();

    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    String getName();

    boolean setData(String ip, int port);

    boolean add(String url, String pktName);
}
