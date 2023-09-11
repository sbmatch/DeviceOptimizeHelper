// IDeviceIdleController.aidl
package android.os;

interface IDeviceIdleController {
      void removePowerSaveWhitelistApp(String name);
      void removeSystemPowerWhitelistApp(String name);
      String[] getUserPowerWhitelist();
      String[] getSystemPowerWhitelist();
      String[] getRemovedSystemPowerWhitelistApps();
}