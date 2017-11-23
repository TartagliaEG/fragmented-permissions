package br.com.tartagliaeg.fragmented.permissions.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by tartagle on 23/11/2017.
 * ...
 */
public class PermissionManager extends Fragment implements IPermissionManager {
  private static final String TAG = PermissionManager.class.getName();

  private static final String SP_NAME = TAG + ".PERMISSIONS_SHARED_PREFERENCES";
  private static final String SP_PERMISSION_NAME = TAG + ".PERMISSION_NAME";
  private static final String SP_PERMISSION_GRANTED = TAG + ".PERMISSION_GRANTED";
  private static final String SP_PERMISSION_ASKED = TAG + ".PERMISSION_ASKED";
  private static final String SP_PERMISSION_NOT_ASK_AGAIN = TAG + ".PERMISSION_NOT_ASK_AGAIN";

  private static final int RC_ASK_FOR_PERMISSION = 2001;
  public static final String FRAGMENT_TAG = "Fragment:" + TAG;

  private static final String SS_IS_WAITING_PERMISSION = TAG + ".IS_WAITING_PERMISSION";
  private boolean mIsWaitingPermission = false;

  private PublishSubject<IPermissionStore> mPermissionsPublisher;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    if (savedInstanceState != null)
      mIsWaitingPermission = savedInstanceState.getBoolean(SS_IS_WAITING_PERMISSION, false);

    super.onCreate(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    mPermissionsPublisher = PublishSubject.create();
  }

  @Override
  public void onStop() {
    super.onStop();
    mPermissionsPublisher.onComplete();
    mPermissionsPublisher = null;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putBoolean(SS_IS_WAITING_PERMISSION, mIsWaitingPermission);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode != RC_ASK_FOR_PERMISSION)
      return;

    for (int i = 0; i < permissions.length; i++) {
      int grantedResult = grantResults[i];
      String permission = permissions[i];

      boolean notAskAgain = false;
      boolean granted = grantedResult == PackageManager.PERMISSION_GRANTED;

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !granted) {
        notAskAgain = !getActivity().shouldShowRequestPermissionRationale(permission);
      }

      Permission perm = new Permission(permission, granted, true, notAskAgain);
      this.savePermission(perm);
    }

    mPermissionsPublisher.onNext(this);
    mPermissionsPublisher.onComplete();
    mPermissionsPublisher = PublishSubject.create();
    mIsWaitingPermission = false;
  }

  /**
   * Ask the given permissions to Android. It will prompt the user to give the requested permissions.
   *
   * @param permissions - A list of permission names. The permission names can be found on Manifest.permission.*
   * @return An observable that will be called when the permissions request receives a result.
   */
  @Override
  public Observable<IPermissionStore> askForPermissions(String... permissions) {
    if (mIsWaitingPermission)
      return mPermissionsPublisher;

    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
      return Observable.just((IPermissionStore) this);

    mIsWaitingPermission = true;
    this.requestPermissions(permissions, RC_ASK_FOR_PERMISSION);

    return mPermissionsPublisher;
  }

  @Override
  public Observable<IPermissionStore> askForNotAskedPermissions(String... permissions) {
    List<String> notAsked = new ArrayList<>();

    for (String permission : permissions) {
      Permission perm = retrievePermission(permission);

      if (!perm.isAsked())
        notAsked.add(permission);
    }

    if (notAsked.size() == 0)
      return Observable.just((IPermissionStore) this);

    return askForPermissions(notAsked.toArray(new String[notAsked.size()]));
  }


  @Override
  public Permission retrievePermission(String permissionName) {
    SharedPreferences pref = this.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    boolean permissionGranted = true;

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      permissionGranted = this.getContext().checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    return new Permission(
        pref.getString(SP_PERMISSION_NAME + permissionName, permissionName),
        permissionGranted,
        pref.getBoolean(SP_PERMISSION_ASKED + permissionName, false),
        pref.getBoolean(SP_PERMISSION_NOT_ASK_AGAIN + permissionName, false)
    );
  }


  private void savePermission(Permission perm) {
    String name = perm.getPermissionName();

    getActivity().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(SP_PERMISSION_NAME + name, perm.getPermissionName())
        .putBoolean(SP_PERMISSION_NOT_ASK_AGAIN + name, perm.isNotAskAgain())
        .putBoolean(SP_PERMISSION_GRANTED + name, perm.isGranted())
        .putBoolean(SP_PERMISSION_ASKED + name, perm.isAsked())
        .apply();
  }
}
