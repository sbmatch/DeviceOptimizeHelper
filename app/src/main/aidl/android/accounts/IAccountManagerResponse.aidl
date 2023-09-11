package android.accounts;

oneway interface IAccountManagerResponse {
    void onResult(in Bundle value);
    void onError(int errorCode, String errorMessage);
}