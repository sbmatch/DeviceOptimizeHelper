package android.content;

import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;

public interface IContentProvider extends IInterface {
    Bundle call(AttributionSource attributionSource, String str, String str2, String str3, Bundle bundle) throws RemoteException;
}
