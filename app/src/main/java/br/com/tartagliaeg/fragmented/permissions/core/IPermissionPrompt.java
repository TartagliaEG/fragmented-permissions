package br.com.tartagliaeg.fragmented.permissions.core;

import io.reactivex.Observable;

/**
 * Created by tartagle on 23/11/2017.
 * ...
 */
public interface IPermissionPrompt {
  Observable<IPermissionStore> askForPermissions(String... permissions);
  Observable<IPermissionStore> askForNotAskedPermissions(String... permissions);
}
