package uk.ac.cam.db538.cryptosms;

import java.lang.Throwable;

public interface IErrorCallback {
  public void onError(Throwable ex);
}
