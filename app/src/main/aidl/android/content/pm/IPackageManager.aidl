// IPackageManager.aidl
package android.content.pm;

// Declare any non-default types here with import statements

interface IPackageManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     int checkPermission(String permName, String pkgName, int userId);
}