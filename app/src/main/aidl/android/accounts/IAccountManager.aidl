package android.accounts;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;

interface IAccountManager {

 boolean removeAccountExplicitly(in Account account);
 void removeAccountAsUser(in IAccountManagerResponse response, in Account account,boolean expectActivityLaunch, int userId);
 Account[] getAccountsAsUser(String accountType, int userId, String opPackageName);
 Account[] getAccountsForPackage(String packageName, int uid, String opPackageName);

}