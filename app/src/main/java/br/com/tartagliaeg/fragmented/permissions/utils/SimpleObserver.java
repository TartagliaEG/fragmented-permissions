package br.com.tartagliaeg.fragmented.permissions.utils;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by tartagle on 23/11/2017.
 * ...
 */
public abstract class SimpleObserver<T> implements Observer<T> {
  @Override
  public void onSubscribe(@NonNull Disposable d) {

  }

  @Override
  public void onError(@NonNull Throwable e) {
    throw new RuntimeException(e);
  }

  @Override
  public void onComplete() {

  }
}
