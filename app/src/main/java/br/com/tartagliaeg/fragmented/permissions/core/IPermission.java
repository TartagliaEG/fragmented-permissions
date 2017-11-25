package br.com.tartagliaeg.fragmented.permissions.core;


import io.reactivex.Observable;

/**
 * Created by tartaglia on 23/11/2017.
 * ...
 */
public interface IPermission  {
  String getPermissionName();
  boolean isGranted();
  boolean isNotAskAgain();
  boolean isAsked();

  interface Requester {
    Observable<IPermission.Retriever> askForPermissions(String... permissions);
    Observable<IPermission.Retriever> askForNotAskedPermissions(String... permissions);
  }

  interface Retriever {
    Permission retrievePermission(String permissionName);
  }

  interface Manager extends Requester, Retriever {
    void handlePermissionResult(String[] permissions, int[] granted);
  }

  interface Store {
    void savePermission(Permission perm);
    boolean isPermissionAsked(String permissionName);
    boolean isPermissionNotAskAgain(String permissionName);
    boolean isPermissionGranted(String permissionName);
  }

  interface Fragment {
    boolean canAskPermissionAgain(String permissionName);
    void showPermissionDialog(String[] permissions);
  }

}
