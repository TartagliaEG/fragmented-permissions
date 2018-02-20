package br.com.tartagliaeg.fragmented.permissions.core;


import android.Manifest;
import android.content.pm.PackageManager;

import io.reactivex.Observable;

/**
 * Created by tartaglia on 23/11/2017.
 * The Permission model interfaces. This file exposes all Interfaces used to ask, store and retrieve
 * permissions.
 */
public interface IPermission {
  String getPermissionName();
  boolean isGranted();
  boolean isNotAskAgain();
  boolean isAsked();

  /**
   * Exposes methods for asking permissions.
   */
  interface Requester {
    /**
     * Should ask for the given permissions.
     *
     * @param permissions a list of permissions to ask for. The permission names could be found at Manifest.permission class. {@link Manifest.permission}
     * @return an Observable holding the Permission retrieve instance. You could use it to check whether the permission were granted or not.
     */
    Observable<IPermission.Retriever> askForPermissions(String... permissions);

    /**
     * Ask for the given permission only if it weren't asked yet.
     *
     * @param permissions a list of permissions to ask for. The permission names could be found at Manifest.permission class. {@link Manifest.permission}
     * @return an Observable holding the Permission retrieve instance. You could use it to check whether the permission were granted or not.
     */
    Observable<IPermission.Retriever> askForNotAskedPermissions(String... permissions);
  }


  /**
   * Exposes methods to retrieve permissions info.
   */
  interface Retriever {
    /**
     * Retrieve the given permission by name. This method is not supposed to ask for permission, it should retrieve the current permission state;
     * @param permissionName a permission name. The permission names could be found at Manifest.permission class. {@link Manifest.permission}
     * @return The Permission state mapped by the given permissionName.
     */
    Permission retrievePermission(String permissionName);
  }

  /**
   * Exposes methods to handle permission results.
   */
  interface Manager extends Requester, Retriever {
    /**
     * Handle permission results.
     * @param permissions The permissions names.
     * @param granted An array of results specifying whether the permission were granted or not. {@link PackageManager#PERMISSION_GRANTED}, {@link PackageManager#PERMISSION_DENIED}
     */
    void handlePermissionResult(String[] permissions, int[] granted);
  }

  /**
   * Exposes methods to persist and retrieve permission state
   */
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
